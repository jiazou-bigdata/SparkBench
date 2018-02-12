user=$1
key=$2
serverlist=$3
#Step1. create a serverlist on each node, including the hostname of all other nodes
#Step2. add ip-hostname mapping for all nodes into each node's /etc/hosts file
#Step3. copy aaa10.pem to every node
#Step4. setup password-less ssh
rm ~/.ssh/known_hosts
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
sudo chmod -R 700 .ssh
sudo chmod -R 640 .ssh/authorized_keys 
./scpWorkers.sh $user $key $serverlist ~/.ssh/id_rsa.pub ~/id_rsa.pub
./sshWorkers.sh $user $key $serverlist 'cat ~/id_rsa.pub >> ~/.ssh/authorized_keys'
./sshWorkers.sh $user $key $serverlist 'chmod -R 700 .ssh'
./sshWorkers.sh $user $key $serverlist 'chmod -R 640 .ssh/authorized_keys'
