#by Jia

import tensorflow as tf

cluster = {

             'master': ['172.30.4.109:1111'],

             'ps': ['172.30.4.109:2222'],

             'worker': ['172.30.4.242:2222', '172.30.4.69:2222']

}

server = tf.train.Server(cluster, job_name='ps', task_index=0)

server.join()
