#/usr/bin/python3

import tensorflow as tf

K = 4    # K classes
km = tf.contrib.factorization.KMeansClustering(num_clusters=K)

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
