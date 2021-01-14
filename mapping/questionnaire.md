# Questionnaire

See [Questionnaire Response](questionnaire-response.md) for create, read, and update semantics

[Composite](https://www.hl7.org/fhir/r4/search.html#composite) search for questionnaires with
[context-type-value](https://www.hl7.org/fhir/r4/questionnaire.html#search)) containing clinic
`https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12975`:

```
GET [base]/Questionnaire?context-type-value=venue$https://staff.apps.va.gov/VistaEmrService/clinics|534/12975
```

Search for questionnaires with context-type-value containing at least one of these clinics:
- `https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12974`
- `https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12975`
- `https://veteran.apps.va.gov/cdw/v3/facilities/534/clinics/12976`

```
GET [base]/Questionnaire?context-type-value=
  venue$https://staff.apps.va.gov/VistaEmrService/clinics|534/12974,
  venue$https://staff.apps.va.gov/VistaEmrService/clinics|534/12975,
  venue$https://staff.apps.va.gov/VistaEmrService/clinics|534/12976
```

System value may also be omitted from the search:
```
GET [base]/Questionnaire?context-type-value=venue$534/12975
GET [base]/Questionnaire?context-type-value=venue$534/12974,venue$534/12975,venue$534/12976
```

```
{
  "resourceType" : "Questionnaire",
  "id" : "a4edd60a-f142-4547-8a9e-02a6bba76bcc",
  "meta" : {
    "tag" : [
      {
        "system" : "https://api.va.gov/services/pgd",
        "code" : "66a5960c-68ee-4689-88ae-4c7cccf7ca79",
        "display" : "VA GOV CLIPBOARD"
      }
    ]
  },
  "identifier" : [
    {
      "system" : "https://api.va.gov/services/va_facilities/v0/facilities",
      "value" : "vha_534"
    }
  ],
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
            "system" : "https://staff.apps.va.gov/VistaEmrService/clinics",
            "code" : "534/12975"
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
