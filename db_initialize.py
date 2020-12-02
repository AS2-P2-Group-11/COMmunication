import requests
api = "http://127.0.0.1:9000"
headers = {
    "accept": "application/json",
    "Content-Type": "application/json"
}
products = {
    "laptop": [
        {
            "name": "macbook",
            "price": 1500
        },
        {
            "name": "lenovo",
            "price": 1300
        },
        {
            "name": "dell",
            "price": 1400
        }
    ],
    "mobile": [
        {
            "name": "iphone",
            "price": 800
        },
        {
            "name": "samsung",
            "price": 700
        },
    ],
    "tablet": [
        {
            "name": "ipad",
            "price": 1100
        },
        {
            "name": "galaxy note",
            "price": 1000
        },
    ]
}
check_for_result = requests.get(api+"/categories/").json()
if len(check_for_result) == 0:
    for category, list_prod in products.items():
        test = requests.post(api+"/categories/", json={"name": str(category)}, headers=headers )
        for product in list_prod:
            requests.post(api+"/category_by_name/"+category+"/items", json=product, headers=headers)
