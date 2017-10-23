# MoreThanJavanaise

Javanaise V1 fonctionnel et testé<br>
quelques JUnit tests disponible<br>
1 burn test (synchronisation sur un jvnobjet par plusieurs processus puis accès en lecture/ecriture concurrent)<br>
   ->créer le jar (main dans AutonomousTesterManager) puis lancer burntest.sh
   
<h2>Shéma Architechture</h2>
+------------------------------------------------------------+<br>
|                                                            |<br>
| +------------+     +---------+     +-------------------+   |<br>
| |            |     |         |     |                   |   |<br>
| |   CLIENT   +----->  Proxy  +----->   Local Ser^eur   |   |<br>
| |            |     |         |     |                   |   |<br>
| +------------+     +---------+     +---------+---------+   |<br>
|                                              |             |<br>
|                                              |             |<br>
|                                              |             |<br>
|                                    +---------v---------+   |<br>
|                                    |                   |   |<br>
|    JVM client                      |   Call Handler    |   |<br>
|                                    |                   |   |<br>
|                                    +--+----------------+   |<br>
|                                       |                    |<br>
+------------------------------------------------------------+<br>
                                        |<br>
          +-----------------+-----------+-----------------+<br>
          |                 |                             |<br>
          |                 |                             |<br>
 +----------------------------------------------+ +----------------------------------------------+<br>
 | +------v-----+ +---------v--+ +------------+ | | +-----v------+ +------------+ +------------+ |<br>
 | |            | |            | |            | | | |            | |            | |            | |<br>
 | |            | |            | |            | | | |            | |            | |            | |<br>
 | |            | |Coordinateur| |Coordinateur| | | |Coordinateur| |            | |Coordinateur| |<br>
 | |LoadBalancer| |            | |            | | | |            | |LoadBalancer| |            | |<br>
 | |            | |   id%n=0   | |   id%n=1   | | | |   id%n=1   | |            | |   id%n=0   | |<br>
 | |   Master   | |            | |            | | | |            | |   Slave    | |            | |<br>
 | |            | |   Master   | |   Slave    | | | |   Master   | |            | |   Slave    | |<br>
 | |            | |            | |            | | | |            | |            | |            | |<br>
 | |            | |            | |            | | | |            | |            | |            | |<br>
 | +------------+ +------------+ +------------+ | | +------------+ +------------+ +------------+ |<br>
 | +------------------------------------------+ | | +------------------------------------------+ |<br>
 | |                                          | | | |                                          | |<br>
 | |              Remote Physical             | | | |              Remote Physical             | |<br>
 | |                                          | | | |                                          | |<br>
 | +------------------------------------------+ | | +------------------------------------------+ |<br>
 |                                              | |                                              |<br>
 |             Jvm Serveur Distant              | |              JVM serveur Distant             |<br>
 |                                              | |                                              |<br>
 +----------------------------------------------+ +----------------------------------------------+<br>
