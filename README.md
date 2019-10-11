# Request Generator

## 1. Test the Sample Code

### 1.1 Build project

Build the project by using <code>mvn install</code>. The artifact shall be generated under <code>target</code>

### 1.2 Run the jar

Make sure <code>resources</code> folder is placed at the same folder with the <code>jar</code> file.

#### Random request generation

The following command generate random request. You can specify the <code>number of requests</code> needs to be generated, otherwise the generator uses the value specified in the configurate file (<code>resources/config.properties</code>) ***NOTE:*** the number of requests is specified per thread.

```bash
java -jar RequestGenerator.jar -r <filename> [number of requests]
```

#### Request generation with fixed order

First, use the following command to generate a dataset.

```bash
java -jar RequestGenerator.jar -f <src> <dataset>
```

Then feed the generator with the dataset: 

```bash
java -jar RequestGenerator.jar -s <dataset>
```

## 2. Use the Sample Code

#### Implement callback

The callback provides a generated request and the id of which thread generates it.

```java
    RequestThread.RequestGenerateThreadCallBack callBack = new RequestThread.RequestGenerateThreadCallBack() {
        @Override
        public void onRequestGenerated(Request request, int threadId) {
            System.out.println("thread[" + threadId + "]: " +  request);
        }
    }
```

#### Declare a generator

There are two types of generator: <code>ClientRequestGenerator</code> and <code>SequentialRequestGenerator</code>, corresponding to what has been described on [section 1.2](run-the-jar). You can implement your own generator by extending the abstract class <code>RequestGenerator</code>.

```java
    RequestGenerator generator = new ClientRequestGenerator("<scr filename>");
```

#### Start generator service

Pass the <code>callback</code> and the <code>generator</code> to the service. Parameters <code>numThreads</code>, <code>interarrivalRate</code> and <code>numOfRequests</code> can be read from the configuration file

```java        
    RequestService service = new RequestService(numThreads,
            interarrivalRate,
            numOfRequests,
            generator,
            callBack);

    service.start();
```

## 3. The Configuration File

The configuration file contains the following parameters:

* <code>read_write_inter_arrival_rate</code>: uses possion distribution. rate = number of requests / total time in ms. For example, 5000 requests in 18 minutes, the rate would be = 5000 / (18 * 60 * 1000) = 0.00463
* <code>request_distribution</code>: value can be [uniform|zipf]
* <code>read_write_ratio</code>: percentage of read and write, sum to 1
* <code>alpha</code>: used for zipf
* <code>number_threads</code>
* <code>num_of_requests</code>: number of requests per thread to generate, use -1 for infinite requests
