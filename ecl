#!/bin/bash

git add -A
git commit -m "Commit all user changes before applying patch"
git am < ecl-patch/0002-Enable-utilisation-of-commercial-licenses.patch
