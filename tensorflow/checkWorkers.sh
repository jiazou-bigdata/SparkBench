#!/bin/bash
# by Jia


user=$1
key=$2
serverlist=$3


arr=($(awk '{print $0}' $serverlist))
length=${#arr[@]}
echo "There are $length servers"
for (( i=0 ; i<$length ; i++ ))
do
        host=${arr[i]}
        index=$i
        echo -e "\n+++++++++++ reconfig: $host"
        command="ps aux | grep kmeans"
        echo $command
        ssh -i $key $user@$host $command
        command="cat log.out"
        echo $command
        ssh -i $key $user@$host $command
done
