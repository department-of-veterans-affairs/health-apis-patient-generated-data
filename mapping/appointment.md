# QuestionnaireResponse and Appointment

Example for saving a questionnaire-response that references a FHIR appointment

### Appointment

- Backed by CDW
- `https://sandbox-api.va.gov/services/fhir/v0/r4/Appointment/I2-ZGHFNASLCRYERJF3R5KUT2JTEQAASLOKBKX5BCCJWT7U7G2DBEWQ0000`

```
{
  "resourceType" : "Appointment",
  "id" : "I2-ZGHFNASLCRYERJF3R5KUT2JTEQAASLOKBKX5BCCJWT7U7G2DBEWQ0000",
  "status" : "fulfilled",
  "description" : "Scheduled Visit",
  "start" : "2020-10-02T13:30:00Z",
  "end" : "2020-10-02T14:00:00Z",
  "minutesDuration" : 30,
  "created" : "2020-10-02",
  "comment" : "PID: 10/2/20  LS: 9/16/20  OVERBOOK APPROVED BY CHAN  F2F",
  "participant" : [
    {
      "actor" : {
        "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Location/I2-2FPCKUIXVR7RJLLG34XVWGZERM000000",
        "display" : "MENTAL HEALTH SERVICES"
      },
      "status" : "accepted"
    },
    {
      "actor" : {
        "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Patient/1011537977V693883",
        "display" : "Kautzer186, Carlita746"
      },
      "status" : "accepted"
    }
  ]
}
```

Appointment has reference to a participant location (clinic), which in turn references managing organization (facility):

```
{
  "resourceType" : "Location",
  "id" : "I2-2FPCKUIXVR7RJLLG34XVWGZERM000000",
  "identifier" : [
    {
      "system" : "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier",
      "value" : "vha_688_3485"
    }
  ],
  "status" : "active",
  "name" : "MENTAL HEALTH SERVICES",
  "description" : "MENTAL HEALTH SERVICES",
  "mode" : "instance",
  "type" : [
    {
      "coding" : [
        {
          "display" : "PSYCHIATRY CLINIC"
        }
      ],
      "text" : "PSYCHIATRY CLINIC"
    }
  ],
  "telecom" : [
    {
      "system" : "phone",
      "value" : "202-745-8267"
    }
  ],
  "address" : {
    "text" : "50 IRVING STREET, NORTHWEST WASHINGTON DC 20422",
    "line" : [
      "50 IRVING STREET, NORTHWEST"
    ],
    "city" : "WASHINGTON",
    "state" : "DC",
    "postalCode" : "20422"
  },
  "physicalType" : {
    "coding" : [
      {
        "display" : "1ST FLOOR, 1D107"
      }
    ],
    "text" : "1ST FLOOR, 1D107"
  },
  "managingOrganization" : {
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Organization/I2-YCRI7L52BYQL63ZFALGWIWF2WU000000",
    "display" : "WASHINGTON VA MEDICAL CENTER"
  }
}
```

```
{
  "resourceType" : "Organization",
  "id" : "I2-YCRI7L52BYQL63ZFALGWIWF2WU000000",
  "identifier" : [
    {
      "system" : "http://hl7.org/fhir/sid/us-npi",
      "value" : "8223895021"
    },
    {
      "use" : "usual",
      "type" : {
        "coding" : [
          {
            "system" : "http://terminology.hl7.org/CodeSystem/v2-0203",
            "code" : "FI",
            "display" : "Facility ID"
          }
        ],
        "text" : "Facility ID"
      },
      "system" : "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier",
      "value" : "vha_688"
    }
  ],
  "active" : true,
  "name" : "WASHINGTON VA MEDICAL CENTER",
  "telecom" : [
    {
      "system" : "phone",
      "value" : "202-745-8000"
    },
    {
      "system" : "phone",
      "value" : "202-745-8588"
    },
    {
      "system" : "fax",
      "value" : "202-745-8530"
    }
  ],
  "address" : [
    {
      "text" : "50 IRVING STREET NORTHWEST WASHINGTON DC 20422-0001",
      "line" : [
        "50 IRVING STREET",
        "NORTHWEST"
      ],
      "city" : "WASHINGTON",
      "state" : "DC",
      "postalCode" : "20422-0001"
    }
  ]
}
```

### QuestionnaireResponse

- `https://sandbox-api.va.gov/services/pgd/v0/r4/QuestionnaireResponse/e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9`
- `subject` is appointment reference
- `source` is patient reference

```
{
  "resourceType" : "QuestionnaireResponse",
  "id" : "e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9",
  "meta" : {
    "tag" : [
      {
        "system" : "https://api.va.gov/services/pgd",
        "code" : "66a5960c-68ee-4689-88ae-4c7cccf7ca79",
        "display" : "VA GOV CLIPBOARD"
      }
    ]
  },
  "text" : {
    "status" : "generated",
    "div" : "<div><h1>Pre-Visit Questionnaire</h1></div>"
  },
  "questionnaire" : "Questionnaire/a4edd60a-f142-4547-8a9e-02a6bba76bcc",
  "status" : "completed",
  "subject" : {
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Appointment/I2-ZGHFNASLCRYERJF3R5KUT2JTEQAASLOKBKX5BCCJWT7U7G2DBEWQ0000"
  },
  "authored" : "2020-08-20",
  "source" : {
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Patient/1011537977V693883"
  },
  "item" : [
    {
      "linkId" : "01",
      "text" : "What is the reason for this appointment?",
      "answer" : [
        {
          "valueString" : "Eye is hurting"
        }
      ]
    },
    {
      "linkId" : "02",
      "text" : "Are there any additional details you'd like to share with your provider about this appointment?",
      "answer" : [
        {
          "valueString" : "cant look into the sun"
        }
      ]
    },
    {
      "linkId" : "03",
      "text" : "Are there any life events that are positively or negatively affecting your health?",
      "answer" : [
        {
          "valueString" : "no"
        }
      ]
    },
    {
      "linkId" : "04",
      "text" : "Do you have other questions you want to ask your provider?",
      "answer" : [
        {
          "valueString" : "Is this covered by my VA Benfits?"
        }
      ]
    }
  ]
}
```

### Example Searches

Search `Location` by facility:

```
Location?identifier=vha_688
```

Search `Location` by clinic:

```
Location?identifier=vha_688_3343
```

Search `Appointment` by patient and start time:

```
Appointment?patient=1011537977V693883&date=eq2017-05-26
```

To search `Appointment` by clinic, search by `Location` first:

```
Location?identifier=vha_688_3343
extract location ID from bundle
Appointment?location=I2-LOCATIONID
```

Search `QuestionnaireResponse` by patient and authored date:

```
QuestionnaireResponse?source=1011537977V693883&authored=2020-08-20
```

To search QuestionnaireResponse by appointment details, search `Location` and `Appointment` first:

```
Location?identifier=vha_688_3343
Extract location ID from bundle
Appointment?patient=1011537977V693883&date=eq2017-05-26&location=I2-LOCATIONID
Extract appointment ID from bundle
QuestionnaireResponse?source=1011537977V693883&subject=I2-APPOINTMENTID
```
