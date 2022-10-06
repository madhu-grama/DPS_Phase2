import os
import subprocess
# import timeit
from urllib import request
import boto3
import time

request_queue = "https://sqs.us-east-1.amazonaws.com/************/RequestQueue"
response_queue = 'https://sqs.us-east-1.amazonaws.com/************/ResponseQueue'
input_s3_bucket = "proj-input-bucket"
output_s3_bucket = 'proj-output-bucket'
s3_bucket = boto3.client('s3', region_name='us-east-1')
sqs_client = boto3.client('sqs', region_name='us-east-1')
ec2_instance = boto3.resource('ec2', region_name='us-east-1')

# Read from the SQS request queue and run the deep learning face recognition model
def queue_processor(req):
    receipt_handle = req['Messages'][0]['ReceiptHandle']
    body = req['Messages'][0]['Body']
    # print(body.split('/'))
    filename = body.split('/')[3]

    s3_bucket.download_file(input_s3_bucket, filename, filename)
    os.system('python3 face_recognition.py ' + filename)
    f = open('result.txt', "r")
    val = f.readline()
    f.close()
    key = filename.split('.')[0]
    output = (key, val, receipt_handle)
    return output

# Send the result back to the web tier in the SQS response queue
def put_to_response_queue(obj_set):
    str_obj_set = str(obj_set).replace("'", "")
    response = sqs_client.send_message(QueueUrl=response_queue,
                                       MessageBody=str_obj_set)

# Store the result in the output S3 bucket
def store_in_bucket(obj_set):
    str_obj_set = str(obj_set).replace("'","")
    s3_bucket.put_object(Bucket=output_s3_bucket, Key=obj_set[0], Body=str_obj_set)

# Delete the message from request queue after processing
def purge_queue(receipt_handle):
    sqs_client.delete_message(QueueUrl=request_queue, ReceiptHandle=receipt_handle)


while (1):
    time.sleep(3.1)
    # start = timeit.default_timer()
    req = sqs_client.receive_message(
        QueueUrl=request_queue,
        MaxNumberOfMessages=1,
        WaitTimeSeconds=0,
    )
    messages_in_queue = len(req.get('Messages', []))
    # print(messages_in_queue)
    if messages_in_queue > 0:
        result = queue_processor(req)
        obj = result[0:2]
        receipt_handle = result[2]
        store_in_bucket(obj)
        put_to_response_queue(obj)
        purge_queue(receipt_handle)
    else:
        # stop = timeit.default_timer()
        # print('Time: ', stop - start)

        # To check the running instances
        instance_id = subprocess.check_output \
            (["wget", "-q", "-O", "-", "http://***.***.***.***/latest/meta-data/instance-id"]).decode("utf-8")
        instance_id = [instance_id]
        ec2_instance.instances.filter(InstanceIds=instance_id).terminate()
        exit()
