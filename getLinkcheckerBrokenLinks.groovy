/**********************************************************************
 * getLinkcheckerBrokenLinks.groovy
 *********************************************************************/

import javax.jcr.NodeIterator      // just for clarity; already on classpath

def localDebug = true
def d = { msg -> if (localDebug) out.println(msg); log.debug("[invalid-link-report] $msg") }

try {
    d ">>> Script started @ ${new Date()}"

    /* --------------------------------------------------------------
       Query: every node under /var/linkchecker with valid = false.
       Most environments store them as nt:unstructured, but we can
       query nt:base so the node type doesn’t matter.
     -------------------------------------------------------------- */
    def sql = """
        SELECT * 
        FROM [nt:base] AS l
        WHERE ISDESCENDANTNODE(l, '/var/linkchecker')
          AND l.[valid] = false
    """

    NodeIterator it = sql2Query(sql)    // helper gives us the iterator
    d "Iterator class: ${it.class.name}"
    d "Approx. size (may be -1): ${it.size}"

    out.println "Path,Link,LastChecked"

    /* --------------------------------------------------------------
       Groovy can ‘each’ a JCR NodeIterator directly.
     -------------------------------------------------------------- */
    it.each { node ->
        def linkProp       = node.hasProperty('link')        ? node.getProperty('link').string      : ''
        def lastCheckedCal = node.hasProperty('lastChecked') ? node.getProperty('lastChecked').date : null
        def lastCheckedStr = lastCheckedCal ? lastCheckedCal.time : ''

        out.println "${node.path},${linkProp},${lastCheckedStr}"
    }

    d ">>> Script finished OK"

} catch (Exception e) {
    log.error("Invalid-link report failed", e)
    throw e                                 // makes the console show the stack trace
} finally {
    save()                                  // no writes, but safe to call
    d "Session saved @ ${new Date()}"
}
