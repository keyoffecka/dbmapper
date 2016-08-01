# DBMapper
A helper library to access and update data managed by a data storage system.
This is designed first of all to work with RDBM-like data storages,
but in theory should be easy to extend for work with other types of data storages.

This library defines the API and provides default implementations.

The API introduces two main concepts: Transaction and Storage

## Transaction
Transaction is a set of queries executed on one connection.
If all the queries succeed till the end of a transaction all of them are applied.
If one of the queries fails all previous queries will be rolled back and the transaction ends immediately.
Starting a transaction on the same thread the second time has no effect.
Transaction gets committed after the last execution block has finished.
The latter means also that if you start a transaction the second time,
it is committed only after the first execution block terminates.

## Storage
Storage is actually responsible of providing access to a data storage, it allows to store, modify data or fetch them.
Storages are accessed within transactions and are provided by them.

## Typical use case
The user should create a transaction instance of their choice.
How the instance is created depends on the actual transaction implementation chosen by the user.
The default transaction implementation defines its own concepts of RDBM Engine, SQL connection factory, and connection session.

### Engine creates queries valid for a particular type of RDBMS
Besides the default implementation the library includes MySQL5 engine.
The MySQL5 engine sets timezone for every connection to UTC.
### Connection session maintains connection lifecycle: acquiring and releasing.
The default implementation doesn't cache connections and creates a connection every time a new one is acquired.
To produce connection it uses a connection factory.
### Connection factory produces SQL connections, that are used to execute queries.

After a transaction instance is created it may be used to execute queries against a storage provided by the transaction inside the execution block.