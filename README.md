##ganglia-extract.py

##Location:
https://github.com/jiazou-bigdata/SparkBench/blob/master/scripts/ganglia-extract.py


##Purpose: 

  The script is to poll rrd data from the rrd buffer during the time you run your benchmark.

  All polled rrd data is written to csv files in ganglia-output folder in the current directory.


##Usage: 

to start it (e.g. before you run your benchmark):

python scripts/ganglia-extract.py #ganglia-rrds-directory #interval-to-collect-stats #duration-ahead-of-current-time (optional)

duration-ahead-of-current-time is set to 30 by default, it should be at least twice larger than the ganglia interval (15 by default), otherwise you won't get any qualified data.


to stop it (e.g. after your benchmark has finished):  

CTRL-C and then archive your output data in ganglia-output folder in current directory, otherwise the folder will be overwritten at the beginning of next run, and data will be lost.



##Example

python scripts/ganglia-extract.py /var/lib/ganglia/rrds/TestingCluster 15 

python scripts/ganglia-extract.py /var/lib/ganglia/rrds/TestingCluster 15 60
