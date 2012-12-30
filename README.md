Query
=====

A library that wraps standard SQL code providing an Iterable to be able to stream the results. Inspired by lots of recent [guava](https://code.google.com/p/guava-libraries/) usage.

Future Plans
------------

Some ideas
* Lenient mode where missing columns don't fail but warn instead, might be good for migration of schemas
* Mode where the field name is used if no annotation is present
