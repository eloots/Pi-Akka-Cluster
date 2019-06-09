#!/bin/sh
#
# Entrypoint that adds `host.docker.internal` for Linux
# https://github.com/docker/for-linux/issues/264

HOST_DOMAIN="host.docker.internal"
ping -q -c1 $HOST_DOMAIN > /dev/null 2>&1
if [ $? -ne 0 ]; then
  HOST_IP=$(ip route | awk 'NR==1 {print $3}')
  echo -e "$HOST_IP\t$HOST_DOMAIN" >> /etc/hosts
fi

exec /bin/prometheus "$@"
