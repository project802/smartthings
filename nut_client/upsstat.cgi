#!/bin/sh

echo "HTTP/1.0 200 OK"
echo "Content-type: text/plain"
echo ""
echo "LIST VAR ups" | nc -N 10.0.0.2 3493
