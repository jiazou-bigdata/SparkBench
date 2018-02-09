## To use it you need to modify following in run_kmeans.py

--cluster configuration


--K

For example:

Cluster = {
           'ps': ['10.134.96.57:2222'],

           'master': ['10.134.96.57:1111'],

           'worker': ['10.134.96.47:2222', '10.134.96.153:2222']
        }


K = 10

## Constraint

1. if you are using local filesystem, parameter server must colocate with master server

2. you must copy run_kmeans.py to each node, and also copy file partition to each node

## Usage

Then type commands like following on each node:

PS: python run_kmeans.py --job_name="ps" --task_index=0 

Master: python run_kmeans.py --job_name="master" --task_index=0   --file="c.txt"

Worker0: python run_kmeans.py --job_name="worker" --task_index=0  --file="b.txt"

Worker1: python run_kmeans.py --job_name="worker" --task_index=1  --file="a.txt" 
