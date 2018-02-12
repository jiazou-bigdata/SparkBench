user=$1
key=$2
worker_tag=$3
./getServerlist.sh $worker_tag
~/passfree.sh $user $key serverlist
./setup.sh
./scpWorkers.sh $user $key serverlist ~/run_kmeans.py ~/
