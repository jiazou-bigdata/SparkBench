#by Jia

import tensorflow as tf
import simplejson as json
import os



cluster = {

             'master': ['172.30.4.109:1111'],

             'ps': ['172.30.4.109:2222'],

             'worker': ['172.30.4.245:2222', '172.30.4.70:2222']

}

os.environ['TF_CONFIG'] = json.dumps(

      {'cluster': cluster,

       'environment': 'cloud',

       'task': {'type': 'worker', 'index': 0}

      })

server = tf.train.Server(cluster, job_name='worker', task_index=0)

K = 4
km = tf.contrib.factorization.KMeansClustering(K, '/tmp/test-kmeans-tf-model')


X = [[1.1, 1.2],
     [2.2, 2.5],
     [3.3, 3.6],
     [2.4, 2.7],
     [14.1, 3.2],
     [4.12, 15.2],
     [3.14, 3.6],
     [2.4, 13.7],
     [3.1, 3.2],
     [4.1, 4.2],
     [13.1, 13.2],
     [4.11, 14.2],
     [5.1, 15.2]]

def train_input_fn():
    data = tf.constant(X, tf.float32)
    return (data, None)


# train
km.train(input_fn=train_input_fn, steps=1000)

centers = km.cluster_centers()

print centers
