import requests
api = "http://127.0.0.1:9000"
headers = {
    "accept": "application/json",
    "Content-Type": "application/json"
}
products = [
  {
    "name": "laptop",
    "items": [
      {
        "name": "macbook",
        "price": 1500,
        "synonyms": [
          {
            "synonym": "apple computer",
          },
          {
            "synonym": "mac",
          }
        ]
      },
      {
        "name": "lenovo",
        "price": 1300,
        "synonyms": []
      },
      {
        "name": "dell",
        "price": 1400,
        "synonyms": []
      }
    ],
    "synonyms": [
      {
        "synonym": "computer",
      }
    ]
  },
  {
    "name": "mobile",
    "items": [
      {
        "name": "iphone",
        "price": 800,
        "synonyms": []
      },
      {
        "name": "samsung",
        "price": 700,
        "synonyms": []
      }
    ],
    "synonyms": [
        {
            "synonym": "Mobile phone",
        },
        {
            "synonym": "phone",
        },
        {
            "synonym": "smart phone",
        }
    ]
  },
  {
    "name": "tablet",
    "items": [
      {
        "name": "ipad",
        "price": 1100,
        "synonyms": []
      },
      {
        "name": "galaxy note",
        "price": 1000,
        "synonyms": []
      }
    ],
    "synonyms": [
        {
            "synonym": "Pad",
        },
    ]
  }
]
check_for_result = requests.get(api+"/categories/").json()
if len(check_for_result) == 0:
    for category in products:
        requests.post(api + "/categories/", json={"name": str(category["name"])}, headers=headers)
        for synonym in category['synonyms']:
            requests.post(api + "/category_by_name/"+category['name']+"/synonyms", json={"synonym": str(synonym["synonym"])}, headers=headers)
        for item in category['items']:
            product = {
                "name": item["name"],
                "price": item['price']
            }
            requests.post(api + "/category_by_name/" + category['name'] + "/items", json=product, headers=headers)
            for synonym in item['synonyms']:
                requests.post(api + "/item_by_name/" + item['name'] + "/synonyms",
                              json={"synonym": str(synonym["synonym"])}, headers=headers)
