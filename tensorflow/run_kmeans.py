
#by Jia

import os
import tensorflow as tf
import simplejson as json
import pandas as pd
import numpy as np
from numpy import genfromtxt

#input flags
tf.app.flags.DEFINE_string("job_name", "", "'ps' or 'worker' or 'master'")
tf.app.flags.DEFINE_integer("task_index", 0, "task index of the job name")
FLAGS=tf.app.flags.FLAGS

def my_input_fn(file_path):
    my_data = genfromtxt(file_path, delimiter=" ")
    data = tf.convert_to_tensor(my_data, dtype=tf.float32)
    print "############"
    return (data, None)

def my_batched_input_fn(file_path):
   
    def decode_csv(line):
        record_defaults = [[0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0], [0.0]]
        f0,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10 = tf.decode_csv(line, record_defaults=record_defaults, field_delim=' ')
        return [f0,f1,f2,f3,f4,f5,f6,f7,f8,f9]

    dataset = (tf.data.TextLineDataset(file_path).map(decode_csv))
    dataset = dataset.batch(32)
    iterator = dataset.make_one_shot_iterator()
    batch_features = iterator.get_next()
    data = tf.convert_to_tensor(batch_features, dtype=tf.float32)
    tf.Print(data, [data], message="this is data: ")
    return (data, None)



def main(unused_argv):
    print("Job name: %s" % FLAGS.job_name)
    print("Task index: %d" % FLAGS.task_index)

    cluster = {

             'master': ['172.30.4.109:8888'],

             'ps': ['172.30.4.109:7777'],

             'worker': ['172.30.4.70:3333', '172.30.4.245:4444']

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
         print("To initialize a KMeans model")

         K = 10
         km = tf.contrib.factorization.KMeansClustering(K, '/tmp/test-kmeans-tf-model', use_mini_batch=True)

         # train
         km.train(input_fn=lambda : my_batched_input_fn("xaa"), steps=1000)

    if FLAGS.job_name == 'master':
         centers = km.cluster_centers()
         print centers
    else:
         print "I am done"

if __name__ == "__main__":
  tf.app.run()


