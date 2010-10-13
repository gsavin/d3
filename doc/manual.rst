===============================================================================
 D3 : Dynamic and Decentralized Distribution
===============================================================================

.. contents::


Main Concepts
===============================================================================

The distribution environment is seen as a set of agencies, with all the same
hierarchical status. Each of these agency models a computing resource. In this 
environment, there are identifiable objects which provides features or which
are making some computing job. The agency is also an identifiable object.

Some of these objects can migrate from one agency to another, for example to 
balance the load of machines or to exploit non-serializable objects.


Identifiable Objects
===============================================================================

Such objects are composed of a few basic things:

- a type. There are actually eight kind of identifiable objects:
	- agency;
	- atlas;
	- feature;
	- protocol;
	- entity;
	- future;
	- migration;
	- application.
	
	Each one of this type will be explained in the following.
- an host, which defines who is hosting the object. Entities have the ability
	to migrate from one host to an other, but except for this kind of object 
	the host-part does not change.
- a path, which depending on the class of the object. This part does not change
	during all the identifiable object life. 
- an id, which, combined to the path, identify the object. As for the path, the
	id does not change during all the identifiable object life.
- a set of methods which can be called from anywhere.

Identifiable objects can be access through a URI which looks like:

	``type://host/path/id``

Methods can be called by giving a query to this URI:

	``type://host/path/id?callable=ping``

This will called the method ``ping()`` of the identifiable object. It is
possible to give arguments to the call by adding a ``args=..`` to the query.
As arguments can be serializable objects, we need to provide an object encoding
method which will transform raw-data to valid url data. For example:

	``type://host/path/id?callable=ping&data_encoding=HEXABYTES&args=A2F..9FD``

The encoding charset can also be specified with ``..&encoding=UTF-8&..``. The
source of the call is specified with ``..&source=xxx&..`` where ``xxx`` is the
URI of the caller url-encoded. When the caller want a return value, it has to
first create a new future, and then add the URI of this future in the query
with ``..&future=...&..``.

Users do not have to create uri-query by themselves, methods are provided to do
the job.
 

Agency
-------------------------------------------------------------------------------

The agency is the entry point of all other identifiable objects to the
distribution environment.

Atlas
-------------------------------------------------------------------------------

Feature
-------------------------------------------------------------------------------

Discovery
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

D3HTTP
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Protocol
-------------------------------------------------------------------------------

UDP versus TCP
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

XML
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Entity
-------------------------------------------------------------------------------

Future
-------------------------------------------------------------------------------

Migration
-------------------------------------------------------------------------------

Application
-------------------------------------------------------------------------------

Configuration
===============================================================================

Deployment
===============================================================================