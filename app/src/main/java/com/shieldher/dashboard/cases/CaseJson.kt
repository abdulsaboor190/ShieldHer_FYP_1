package com.shieldher.dashboard.cases

import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseItem
import org.json.JSONArray
import org.json.JSONObject

/** Maps between in-memory model objects and their JSON representations. */
internal object CaseJson {

    // ── Case ─────────────────────────────────────────────────────────────────

    fun caseToJson(c: Case): JSONObject = JSONObject().apply {
        put("id", c.id)
        put("title", c.title)
        put("description", c.description)
        put("createdAtMillis", c.createdAtMillis)
        put("isDemo", c.isDemo)
    }

    fun caseFromJson(o: JSONObject) = Case(
        id = o.getString("id"),
        title = o.getString("title"),
        description = o.getString("description"),
        createdAtMillis = o.getLong("createdAtMillis"),
        isDemo = o.optBoolean("isDemo", false)
    )

    // ── CaseItem ──────────────────────────────────────────────────────────────

    fun itemToJson(i: CaseItem): JSONObject = JSONObject().apply {
        put("id", i.id)
        put("caseId", i.caseId)
        if (i.evidenceId != null) put("evidenceId", i.evidenceId) else put("evidenceId", JSONObject.NULL)
        put("note", i.note)
        if (i.incidentDateMillis != null) put("incidentDateMillis", i.incidentDateMillis) else put("incidentDateMillis", JSONObject.NULL)
        put("addedAtMillis", i.addedAtMillis)
        put("isDemo", i.isDemo)
    }

    fun itemFromJson(o: JSONObject) = CaseItem(
        id = o.getString("id"),
        caseId = o.getString("caseId"),
        evidenceId = if (o.isNull("evidenceId")) null else o.getString("evidenceId"),
        note = o.getString("note"),
        incidentDateMillis = if (o.isNull("incidentDateMillis")) null else o.getLong("incidentDateMillis"),
        addedAtMillis = o.getLong("addedAtMillis"),
        isDemo = o.optBoolean("isDemo", false)
    )

    // ── Store document ────────────────────────────────────────────────────────

    fun encode(cases: List<Case>, items: List<CaseItem>): String {
        val casesArr = JSONArray()
        cases.forEach { casesArr.put(caseToJson(it)) }
        val itemsArr = JSONArray()
        items.forEach { itemsArr.put(itemToJson(it)) }
        return JSONObject().apply {
            put("schemaVersion", 1)
            put("cases", casesArr)
            put("items", itemsArr)
        }.toString()
    }

    data class StoreDoc(val cases: List<Case>, val items: List<CaseItem>)

    fun decode(json: String): StoreDoc {
        val root = JSONObject(json)
        val schemaVersion = root.getInt("schemaVersion")
        require(schemaVersion == 1) { "Unknown schema version $schemaVersion" }
        val cases = mutableListOf<Case>()
        val items = mutableListOf<CaseItem>()
        val casesArr = root.getJSONArray("cases")
        for (i in 0 until casesArr.length()) cases.add(caseFromJson(casesArr.getJSONObject(i)))
        val itemsArr = root.getJSONArray("items")
        for (i in 0 until itemsArr.length()) items.add(itemFromJson(itemsArr.getJSONObject(i)))
        return StoreDoc(cases, items)
    }
}
