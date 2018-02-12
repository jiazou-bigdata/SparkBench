
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
tf.app.flags.DEFINE_integer("num_dims", 10, "num dimensions")
tf.app.flags.DEFINE_integer("num_points", 1000000, "num shards")
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

def my_dataset_input_fn(file_path):

    def decode_csv(line):
        record_defaults = [[0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0]]
        f0,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10 = tf.decode_csv(line, record_defaults=record_defaults, field_delim=' ')
        return [f0,f1,f2,f3,f4,f5,f6,f7,f8,f9]

    dataset = (tf.data.TextLineDataset(file_path).map(decode_csv))
#    dataset = dataset.shard(10, 0)
    dataset = dataset.batch(FLAGS.num_points)
    iterator = dataset.make_one_shot_iterator()
    features = iterator.get_next()
    data = tf.convert_to_tensor(features, dtype=tf.float32)
    return (data, None)   

def my_numpy_dataset_input_fn():
    dataset = tf.data.Dataset.from_tensor_slices(tf.random_uniform([FLAGS.num_points,  FLAGS.num_dims]))
    dataset = dataset.batch(FLAGS.num_points)
    iterator = dataset.make_one_shot_iterator()
    features = iterator.get_next()
    data = tf.convert_to_tensor(features, dtype=tf.float32)
    return (data, None)


def my_restore_input_fn():
    data = tf.get_variable("data_"+FLAGS.job_name+str(FLAGS.task_index), [FLAGS.num_points, FLAGS.num_dims], tf.float32)
    saver = tf.train.Saver(tf.global_variables())
    with tf.Session() as sess:
        # Restore variables from disk.
        saver.restore(sess, "./model.ckpt")
        print data.eval()
    return (data, None)

def my_input_fn(data):
    return (data, None)

#def my_placeholder_input_fn():
#    data = tf.placeholder(tf.float32, shape=(1000000,100), name="data")
#    my_data = data
#    with tf.Session() as sess:
#        my_data=sess.run(my_data, feed_dict={data: rand_array_tensor})
#    return (my_data, None)



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

             'worker': ['172.30.4.217:4444', '172.30.4.246:4444', '172.30.4.228:4444', '172.30.4.64:4444', '172.30.4.15:4444', '172.30.4.154:4444', '172.30.4.40:4444', '172.30.4.24:4444', '172.30.4.232:4444', '172.30.4.245:4444']

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
         #input
         data_placeholder = tf.placeholder(tf.float32, shape=(FLAGS.num_points, FLAGS.num_dims), name="data")
         rand_array=np.random.rand(FLAGS.num_points, FLAGS.num_dims)

         print("To initialize a KMeans model")
         K = 100
         if FLAGS.training_mode == 'full':

#__init__(
#    inputs,
#    num_clusters,
#    initial_clusters=RANDOM_INIT,
#    distance_metric=SQUARED_EUCLIDEAN_DISTANCE,
#    use_mini_batch=False,
#    mini_batch_steps_per_iteration=1,
#    random_seed=0,
#    kmeans_plus_plus_num_retries=2,
#    kmc2_chain_length=200
#)
             km = tf.contrib.factorization.KMeans(data_placeholder, K, use_mini_batch=False)
         else:
             km = tf.contrib.factorization.KMeansClustering(K, '/tmp/test-kmeans-tf-model', use_mini_batch=True, mini_batch_steps_per_iteration=FLAGS.mini_batch_size, config=tf.contrib.learn.RunConfig(num_cores=8, save_summary_steps=10000, save_checkpoints_secs=1200, log_step_count_steps=1000))

         start_time = time.time()
         # train
         if FLAGS.training_mode == 'full':

             (all_scores, _, clustering_scores, _, kmeans_init,
              kmeans_training_op) = km.training_graph()
             init = tf.global_variables_initializer()
             sess = tf.Session()
             sess.run(init)
             sess.run(kmeans_init, feed_dict={data_placeholder: rand_array})
             start_time1 = time.time()
             for step in xrange(FLAGS.steps):
                 sess.run(kmeans_training_op, feed_dict={data_placeholder: rand_array})
         else:
             km.train(input_fn=lambda : my_batched_input_fn(FLAGS.file), steps=FLAGS.steps)
         end_time = time.time()
         print "elapsed time: "
         print (end_time - start_time)
         print "step time: "
         print (end_time - start_time1)
    if FLAGS.job_name == 'master':
         print "done"

if __name__ == "__main__":
  tf.app.run()


