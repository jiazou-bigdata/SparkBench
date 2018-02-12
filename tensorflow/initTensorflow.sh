#by Jia


user=$1
key=$2
worker_tag=$3

#to obtain a serverlist
./getServerlist.sh $worker_tag

#to make a pass-free cluster
~/passfree.sh $user $key serverlist

#to install tensorflow and related packages
./setup.sh

#to distribute scripts to workers
./scpWorkers.sh $user $key serverlist ~/run_kmeans.py ~/
