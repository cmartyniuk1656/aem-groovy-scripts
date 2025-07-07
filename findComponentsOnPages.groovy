/**
 * Groovy Console – Find pages that contain a specific component
 * -------------------------------------------------------------
 * 1. Set SEARCH_ROOT 
 * 2. Set TARGET_RESOURCE_TYPE
 * 3. Set REPLICATION_FILTER (ACTIVATED / DEACTIVATED / ANY)
 *
 * Author: Chris Martyniuk
 */

/* ===== CONFIG ======================================================== */
def SEARCH_ROOT          = "/content/echo/master/main/en_us"
def TARGET_RESOURCE_TYPE = "echo/components/content/ut-card"

enum ReplicationFilter { ACTIVATED, DEACTIVATED, ANY }
def REPLICATION_FILTER   = ReplicationFilter.ANY                   // <-- set replication filter
/* ==================================================================== */

import com.day.cq.search.QueryBuilder
import com.day.cq.wcm.api.Page
import com.day.cq.replication.ReplicationStatus

/* --- QueryBuilder search for the component nodes -------------------- */
def hits = createQuery([
        path            : SEARCH_ROOT,
        type            : "nt:base",
        property        : "sling:resourceType",
        "property.value": TARGET_RESOURCE_TYPE,
        "p.limit"       : "-1"
]).result.hits

def resultPages = [] as LinkedHashSet<Page>

/* --- Walk hits > page > replication filter -------------------------- */
hits.each { hit ->
    def res  = hit.resource
    def page = pageManager.getContainingPage(res)
    if (!page) { return }

    if (REPLICATION_FILTER != ReplicationFilter.ANY) {
        def status       = page.contentResource?.adaptTo(ReplicationStatus)
        def isActivated  = status?.activated == true

        if (REPLICATION_FILTER == ReplicationFilter.ACTIVATED && !isActivated)  return
        if (REPLICATION_FILTER == ReplicationFilter.DEACTIVATED &&  isActivated) return
    }
    resultPages << page
}

/* --- Output ---------------------------------------------------------- */
out.println ""
out.println "  Pages under ${SEARCH_ROOT} containing ${TARGET_RESOURCE_TYPE}"
out.println "  Replication filter: ${REPLICATION_FILTER}"
out.println "-----------------------------------------------------------------"
resultPages.each { out.println it.path }
out.println "-----------------------------------------------------------------"
out.println "Total pages found: ${resultPages.size()}"
