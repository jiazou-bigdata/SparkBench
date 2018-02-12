#!/bin/bash
# by Jia


user=$1
key=$2
serverlist=$3
num_points=$4
num_dims=$5

arr=($(awk '{print $0}' $serverlist))
length=${#arr[@]}
echo "There are $length servers"
for (( i=0 ; i<$length ; i++ ))
do
        host=${arr[i]}
        index=$i
        echo -e "\n+++++++++++ reconfig: $host"
        command="nohup python  run_kmeans.py --job_name=worker --task_index=$index --training_mode=full --num_points=$num_points --num_dims=$num_dims >> log.out 2>&1 < /dev/null &"
        echo $command
        ssh -i $key $user@$host $command
done
