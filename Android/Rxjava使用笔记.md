## 对Observable stream的控制
### 创建
* just( ) — convert an object or several objects into an Observable that emits that object or those objects
* from( ) — convert an Iterable, a Future, or an Array into an Observable

* create( ) — advanced use only! create an Observable from scratch by means of a function, consider fromEmitter instead

* fromEmitter() — create safe, backpressure-enabled, unsubscription-supporting Observable via a function and push events.

* defer( ) — do not create the Observable until a Subscriber subscribes; create a fresh Observable on each subscription

* range( ) — create an Observable that emits a range of sequential integers

* interval( ) — create an Observable that emits a sequence of integers spaced by a given time interval

* timer( ) — create an Observable that emits a single item after a given delay

* empty( ) — create an Observable that emits nothing and then completes

* error( ) — create an Observable that emits nothing and then signals an error

* never( ) — create an Observable that emits nothing at all

