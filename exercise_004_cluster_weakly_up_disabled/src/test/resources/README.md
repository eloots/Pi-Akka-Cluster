cluster_weakly_up_disabled

# Disable Weakly-up feature

In this set-up, we disable the so-called `Weakly-up` feature.

If the `Weakly-up` feature is enabled (this is the default), new members who try to join an existing cluster, will move from the `Joining` state to the `Weakly-up` state. `Weakly-up` nodes can participate in offering cluster services, but will not be counted in any decisions of determining quorum (as performed by the Split Brain Resolver).

When, as in this example, the `Weakly-up` feature is disabled, nodes will stay in the `Joining` state until the cluster leader detects cluster state convergence and moves these nodes to the (fully) `Up` state 