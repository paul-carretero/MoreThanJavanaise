#!/bin/bash
printf "\033c"
echo ''
echo '_____________________________________________________________________'
echo '        __                                                           '
echo '        /                                 ,                          '
echo '-------/-----__---------__----__----__-------__----__-               '
echo '      /    /   ) | /  /   ) /   ) /   ) /   (_ ` /___)               '
echo '_(___/____(___(__|/__(___(_/___/_(___(_/___(__)_(___ _               '
echo '                                                                     '
echo '                                                                     '
echo '_____________________________________________________________________'
echo '    ____                                                             '
echo '    /    )             /                    /                        '
echo '---/___ /----__----__-/-----------__----__-/----__----__----__-------'
echo '  /    |   /___) /   /   /   /  /   ) /   /   /   ) /   ) /   '"'"' /   /'
echo '_/_____|__(___ _(___/___(___(__/___/_(___/___(___(_/___/_(___ _(___/_'
echo '                                                                  /  '
echo '                                                              (_ /   '
echo '_____________________________________________________________________'
echo '  ______                      ____                                /  '
echo '    /                         /    )                   /         /   '
echo '---/-------__---__--_/_------/___ /----__----__----__-/---------/--- '
echo '  /      /___) (_ ` /       /    |   /___) /   ) /   /   /   / /     '
echo '_/______(___ _(__)_(_ _____/_____|__(___ _(___(_(___/___(___/_o______'
echo '                                                           /'
echo '                                                       (_ /'
cd build

if [ "`ps aux | grep '[r]miregistry'`" = "" ]
then
    echo " -> Lancement de RMIRegistry"
    eval "rmiregistry&"
    sleep 10
fi

echo " -> Nombre de Worker ?"
read nbWorker
echo " -> Nombre d'itération ?"
read nbIteration

#`gnome-terminal -x sh -c "java -jar main.jar coord ; bash"`

echo " -> Lancement du Manager"
`gnome-terminal -x sh -c "java -jar redundancyTest.jar manager $nbWorker; bash"`

echo " -> Lancement des Tester"
for i in `seq 1 $nbWorker`;
do
	`gnome-terminal -x sh -c "java -jar redundancyTest.jar tester $i $nbWorker $nbIteration; bash"`
done
