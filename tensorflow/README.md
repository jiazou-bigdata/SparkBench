## To use it you need to modify following in run_kmeans.py

--cluster configuration
--input file name
--K

For example:

Cluster = {
           'ps': ['10.134.96.57:2222'],
           'master': ['10.134.96.57:1111'],
           'worker': ['10.134.96.47:2222', '10.134.96.153:2222']
        }

km.train(input_fn=lambda : my_batched_input_fn("xaa"), steps=1000)

K = 10

## Constraint

parameter server must colocate with master server

## Usage

Then type commands like following on each node:

PS: python run_kmeans.py --job_name="ps" --task_index=0 
Master: python run_kmeans.py --job_name="master" --task_index=0
Worker0: python run_kmeans.py --job_name="worker" --task_index=0
Worker1: python run_kmeans.py --job_name="worker" --task_index=1 
