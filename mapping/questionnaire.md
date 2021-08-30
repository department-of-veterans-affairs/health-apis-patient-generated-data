# Questionnaire

See [Questionnaire Response](questionnaire-response.md) for create, read, and update semantics

[Composite](https://www.hl7.org/fhir/r4/search.html#composite) search for questionnaires with
[context-type-value](https://www.hl7.org/fhir/r4/questionnaire.html#search)) containing clinic
`https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12975`:

```
GET [base]/Questionnaire?context-type-value=venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|vha_534_12975
```

Search for questionnaires with context-type-value containing at least one of these clinics:
- `https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12974`
- `https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12975`
- `https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12976`

```
GET [base]/Questionnaire?context-type-value=
  venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|vha_534_12974,
  venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|vha_534_12975,
  venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|vha_534_12976
```

System value may also be omitted from the search:

```
GET [base]/Questionnaire?context-type-value=venue$vha_534_12975
GET [base]/Questionnaire?context-type-value=venue$vha_534_12974,venue$vha_534_12975,venue$vha_534_12976
```

```
{
  "resourceType" : "Questionnaire",
  "id" : "a4edd60a-f142-4547-8a9e-02a6bba76bcc",
  "title" : "VA GOV Pre-Visit Agenda Questionnaire",
  "status" : "active",
  "publisher" : "clipboard.gov.va",
  "useContext" : [
    {
      "code" : {
        "system" : "http://terminology.hl7.org/CodeSystem/usage-context-type",
        "code" : "venue"
      },
      "valueCodeableConcept" : {
        "coding" : [
          {
            "system" : "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier",
            "code" : "vha_534_12975"
          }
        ]
      }
    }
  ],
  "item" : [
    {
      "linkId" : "01",
      "text" : "What is the reason for this appointment?",
      "type" : "string"
    },
    {
      "linkId" : "02",
      "text" : "Are there any additional details you'd like to share with your provider about this appointment?",
      "type" : "text"
    },
    {
      "linkId" : "03",
      "text" : "Are there any life events that are positively or negatively affecting your health?",
      "type" : "text"
    },
    {
      "linkId" : "04",
      "text" : "Do you have other questions you want to ask your provider?",
      "type" : "text"
    }
  ]
}
```
