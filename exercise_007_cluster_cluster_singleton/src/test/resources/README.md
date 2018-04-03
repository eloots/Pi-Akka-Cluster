cluster_singleton

# Cluster Singleton

We introduce a so-called `Cluster Singleton`. As the name suggests, at anyone point in time, one and only one instance of a singleton is running on a cluster.

In a next stage, we will see that if we're uncareful, we may actually end-up in a state where more than one instance of the singleton is running. Obviously, this is faulty and we will see how we can avoid this from happening.
