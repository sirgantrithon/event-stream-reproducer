Overview
========

This project is for reproducing a behavior in Vert.x 3.0-SNAPSHOT as of 16 May 2015.
It performs the following actions:

1.  Creates a clustered Vertx instance
2.  A MessageConsumer<Buffer> is registered, with the bodies of the received messages being
Pumped into a WriteStream<Buffer> (a file)
3.  A file is opened, and its contents are Pumped into a MessageProducer<Buffer> that sends
to the same address as the MessageConsumer
4.  After the file is finished being written, the contents of the original file and the new
file are compared

Running the project
-------------------

The project uses vertx-unit as the execution mechanism, so simply running the maven build
will execute the project.  It may also be imported into an IDE and run from there. An
eclipse project is already provided.
