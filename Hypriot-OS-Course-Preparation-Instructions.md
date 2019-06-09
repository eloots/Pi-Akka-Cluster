## Raspberry-Pi based Akka Cluster workshop preparation instructions

In case you're attending a `Raspberry-Pi based Akka Cluster workshop`, you will need to prepare your laptop to integrate with a pre-installed 5-node cluster you'll be working on.

The following sections give instructions on how to achieve this.

### Adding the cluster nodes to /etc/hosts

On your Mac, add the following entries to your _/etc/hosts_ file:

```
# Cluster #0
192.168.200.10 node-0
192.168.200.11 node-1
192.168.200.12 node-2
192.168.200.13 node-3
192.168.200.14 node-4
```


### Configure password-less login

Next, we set-up password-less login (unless your `ssh` remembers your password, in which case you can skip this step). We assume that we want to log in to account _akkapi_ on a Raspberry Pi node from an account (_userxxx_) on your laptop. In order to set-up password-less login, you need a so-called public/private key pair. If you're unfamiliar with this concept, or don't have such a key pair, first read the paragraph titled _Generating a public/private key pair_ in the Addendum.

Now, proceed by create a `.ssh` folder on the _akkapi_'s home folder on the Pi and copy the public key (`id_rsa.pub`) to a file named `authorized_keys` in the `.ssh` folder. You will have to supply the password for the _akkapi_ account for the two commands launched. On your laptop:

```
[Pi-Akka-Cluster git:(master) ✗ for node in 0 1 2 3 4;do ssh akkapi@node-${node} mkdir -p .ssh; done
akkapi@node-0's password:
```

Copy the contents of your *public key* to the `authorized_keys` file:

```
Pi-Akka-Cluster git:(master) ✗ for node in 0 1 2 3 4;do cat ~/.ssh/id_rsa.pub | ssh akkapi@node-${node} 'cat >> .ssh/authorized_keys'; done
akkapi@node-0's password:
```

With this, you should now be able to log into the _akkapi_ on all nodes from the _userxxx_ account on your laptop without having to enter a password.

## Addendum

### Generating a public/private key pair

We assume that we want to log in to account _akkapi_ on a pi from an account (_userxxx_) on your laptop.

First, we need a pair of authentication keys ***on the laptop***. If these keys have been generated already we can skip the generation of a new pair of authentication keys. If this isn't the case, generate them.

Logged-in on your laptop account, generate a key pair:

```
$ ssh-keygen -t rsa
Generating public/private rsa key pair.
Enter file in which to save the key (/home/userxxx/.ssh/id_rsa): 
Created directory '/home/userxxx/.ssh'.
Enter passphrase (empty for no passphrase): 
Enter same passphrase again: 
Your identification has been saved in /home/userxxx/.ssh/id_rsa.
Your public key has been saved in /home/userxxx/.ssh/id_rsa.pub.
The key fingerprint is:
3e:4f:05:79:3a:9f:96:7c:3b:ad:e9:58:37:bc:37:e4 userxxx@A
``` 

Use an empty passphrase (just hit _Enter_)

As can be seen from the output generated by _ssh-keygen_, a pair of files (`id_rsa` and `id_rsa.pub`) are created in the (hidden) folder `.ssh` in the users home folder.

Let's have a look:

```
$ ls -l ~/.ssh
total 56
-rw-------  1 ericloots  staff   3326 Dec 21  2015 id_rsa
-rw-r--r--  1 ericloots  staff    746 Dec 21  2015 id_rsa.pub
```