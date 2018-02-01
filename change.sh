if [ -f README.md ]; then     sed 's/Location//g' README.md > tmp;     mv tmp README.md; fi
