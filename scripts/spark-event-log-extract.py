import sys
import json


log = open( sys.argv[1], 'r' )
print "TaskID, TaskType, Elapsed, Deserialize, DeserializeCPU, Serialize"
for line in log:
    blob = json.loads( line )
    if blob['Event'] == 'SparkListenerTaskEnd':
        taskType = blob["Task Type"]
        elapsed_ms = int(blob['Task Info']['Finish Time']) -  int(blob['Task Info']['Launch Time'])
        deserialize_ms = int(blob['Task Metrics']['Executor Deserialize Time'])
        deserialize_cpu_ms = int(blob['Task Metrics']['Executor Deserialize CPU Time'])
        result_serialize_ms = int(blob['Task Metrics']['Result Serialization Time'])
        print "%6d, %s, %.2f, %.2f, %.2f, %.2f" % (
            blob['Task Info']['Task ID'],
            taskType,
            elapsed_ms * 1.0,
            deserialize_ms * 1.0,
            deserialize_cpu_ms * 1.0,
            result_serialize_ms * 1.0
            )
