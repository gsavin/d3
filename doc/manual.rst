=======================================================================
 D3 : Dynamic and Decentralized Distribution
=======================================================================

.. contents::


Main Concepts
=======================================================================

The distribution environment is seen as a set of agencies, with all
the same hierarchical status. Each agency is composed of actors which
can do some job, communicate through the network, migrate from one
agency to another... Agency is the main actor and controls all other.


Actors
=======================================================================

Actor is a model of concurrent computation. Unlike a classic object,
an actor has its own execution thread in which it executes its
code. This thread is called the *body* of the actor and has a queue
dedicated to received requests. Invocation of actor methods is done
through a peer of request/future : the invocation is modeled as a
request which is enqueued in the body queue

body
  Body is the execution thread of the actor 

request
  A request models the invocation of a method of the actor. Requests
  are enqueued and handled by the body.

future
  When an entity calls a method of an actor, the result is only
  available when the associated request are handled by the body. A
  future models the future return value of the invocation, 

Actors are divided in two main categories, the one composed of actors
which are located on the machine we are considering and the one
composed of actors located somewhere else. Actors of the first
category are called *local actors* while actors of the second one are
called *remote actors*.

These two categories are reified in the objects:

- ``org.d3.actor.LocalActor``
- ``org.d3.actor.RemoteActor``

There are several types of actor and each one has its own
specifications:

- Agency
- Feature
- Protocol
- Entity

Agency is the main type of actor. There is only one agency by
runtime. It allows the deployment of D3 and the control of other
actors. Features allow to extend features provide by the agency. 

Agency
-----------------------------------------------------------------------

Feature
-----------------------------------------------------------------------

Protocol
-----------------------------------------------------------------------

Entity
-----------------------------------------------------------------------

Remote Actors
-----------------------------------------------------------------------


Mecanisms
=======================================================================

Request and Future
-----------------------------------------------------------------------

Migration
-----------------------------------------------------------------------

Local events dispatching
-----------------------------------------------------------------------

An actor can produced a set of dispatchable events. It has to use the 
interface EventDispatchable.
