
#by Jia

import os
import tensorflow as tf
import simplejson as json
import pandas as pd
import numpy as np
import time
from numpy import genfromtxt

#input flags
tf.app.flags.DEFINE_string("job_name", "", "'ps' or 'worker' or 'master'")
tf.app.flags.DEFINE_integer("task_index", 0, "task index of the job name")
tf.app.flags.DEFINE_string("file","", "input file")
tf.app.flags.DEFINE_string("training_mode", "'full'", "'full' or 'mini'")
tf.app.flags.DEFINE_integer("x", 10, "num dimensions")
tf.app.flags.DEFINE_integer("y", 1000000, "num points")
tf.app.flags.DEFINE_integer("input_batch_size",100, "input_batch_size")
tf.app.flags.DEFINE_integer("mini_batch_size", 100, "mini_batch_size")
tf.app.flags.DEFINE_integer("steps",5, "steps")

FLAGS=tf.app.flags.FLAGS

def my_numpy_input_fn(file_path):
    start_time = time.time()
    my_data = genfromtxt(file_path, delimiter=" ")
    end_time = time.time()
    print("data load time: %d" % (end_time-start_time))
    data = tf.convert_to_tensor(my_data, dtype=tf.float32)
    return (data, None)

def my_input_fn(data):
    return (data, None)   



def my_batched_input_fn(file_path):
   
    def decode_csv(line):
        record_defaults = [[0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0]]
        f0,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10 = tf.decode_csv(line, record_defaults=record_defaults, field_delim=' ')
        return [f0,f1,f2,f3,f4,f5,f6,f7,f8,f9]

    dataset = (tf.data.TextLineDataset(file_path).map(decode_csv))
    dataset = dataset.repeat()
    dataset = dataset.batch(FLAGS.input_batch_size)
    iterator = dataset.make_one_shot_iterator()
    batch_features = iterator.get_next()
    data = tf.convert_to_tensor(batch_features, dtype=tf.float32)
    return (data, None)



def main(unused_argv):
    print("Job name: %s" % FLAGS.job_name)
    print("Task index: %d" % FLAGS.task_index)
    print("File: %s" % FLAGS.file)

    cluster = {

             'master': ['172.30.4.109:8888'],

             'ps': ['172.30.4.109:7777'],

             'worker': ['172.30.4.245:4444']

    }

    os.environ['TF_CONFIG'] = json.dumps(

       {'cluster': cluster,

        'environment': 'cloud',

        'task': {'type': FLAGS.job_name, 'index': FLAGS.task_index}

       })

    server = tf.train.Server(cluster, job_name=FLAGS.job_name, task_index=FLAGS.task_index)

    #TRAINING

    if FLAGS.job_name == 'ps':
         server.join()
    else:
         # inputs
         data1 = tf.placeholder(tf.float32, shape=(1000000,10), name="data")
         data2 = tf.placeholder(tf.float32, shape=(1000000,10), name="data")
         myData = [data1, data2]
         with tf.Session(config=tf.ConfigProto(intra_op_parallelism_threads=12, inter_op_parallelism_threads=12)) as sess:
             rand_array1 = np.random.rand(1000000, 10)
             rand_array2 = np.random.rand(1000000, 10)
             myData=sess.run(myData, feed_dict={data1: rand_array1, data2: rand_array2})

         print myData
         print("To initialize a KMeans model")
         K = 100
         if FLAGS.training_mode == 'full':
             km = tf.contrib.factorization.KMeansClustering(K, '/tmp/test-kmeans-tf-model', use_mini_batch=False, config=tf.contrib.learn.RunConfig(num_cores=8, save_summary_steps=10000, save_checkpoints_secs=1200, log_step_count_steps=1000))
         else:
             km = tf.contrib.factorization.KMeansClustering(K, '/tmp/test-kmeans-tf-model', use_mini_batch=True, mini_batch_steps_per_iteration=FLAGS.mini_batch_size, config=tf.contrib.learn.RunConfig(num_cores=8, save_summary_steps=10000, save_checkpoints_secs=1200, log_step_count_steps=1000))

         start_time = time.time()
         # train
         if FLAGS.training_mode == 'full':
             km.train(input_fn=lambda : my_input_fn(myData), steps=5)
         else:
             km.train(input_fn=lambda : my_batched_input_fn(FLAGS.file), steps=FLAGS.steps)
         end_time = time.time()

    if FLAGS.job_name == 'master':
         centers = km.cluster_centers()
         print centers
         print "elapsed time: "
         print (end_time - start_time)
    else:
         print "elapsed time: "
         print (end_time - start_time)


if __name__ == "__main__":
  tf.app.run()


