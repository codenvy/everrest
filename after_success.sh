#!/bin/bash
wget http://roboconf.net/resources/build/settings.xml
mvn clean deploy -q --settings settings.xml