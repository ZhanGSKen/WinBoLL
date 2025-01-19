#!/usr/bin/bash
jarversion=$1
jarname="libjc-"${jarversion}".jar"

cd output_jars

echo -e "\nTest "${jarname}" Main :"
java -jar ${jarname} "UnitTest"

testclass="TestClassA"
echo -e "\nTest "${jarname}" ${testclass} :"
echo -e "Test 1 :"
java -cp ${jarname} cc.winboll.studio.libjc.${testclass}
echo -e "Test 2 :<Test>"
java -cp ${jarname} cc.winboll.studio.libjc.${testclass} "Test"
echo -e "Test 3 :<UnitTest>"
java -cp ${jarname} cc.winboll.studio.libjc.${testclass} "UnitTest"

testclass="TestClassB"
echo -e "\nTest "${jarname}" ${testclass} :"
echo "Test 1 :"
java -cp ${jarname} cc.winboll.studio.libjc.${testclass}
echo -e "Test 2 :<Test>"
java -cp ${jarname} cc.winboll.studio.libjc.${testclass} "Test"
echo -e "Test 3 :<UnitTest>"
java -cp ${jarname} cc.winboll.studio.libjc.${testclass} "UnitTest"
