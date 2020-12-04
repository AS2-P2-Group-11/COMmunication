package furhatos.app.agent.flow

import com.google.gson.Gson
import furhatos.app.agent.nlu.*
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.*
import khttp.get
import khttp.post

operator fun Int?.plus(other: Int?): Int? = if (this != null && other != null) this + other else null

data class OrderData (
        var status: String? = null,
        var id: Int? = null,
        var date: String? = null,
        var items: List<ItemOrderData>? = null
)
data class ItemOrderData (
        var quantity: Int? = null,
        var id: Int? = null,
        var order_id: Int? = null,
        var item: ItemData? = null
)

//Greeting
val Start = state(Interaction) {
    onEntry {
        random(
                //{   furhat.say("Whazzup dawg?!") },
                //{   furhat.say("Greetings! What an absolute pleasure to meet you.") },
                {   furhat.say("Welcome to Furhat electronics store") }
        )

        goto(ChooseAction)
    }
}

//The "main menu" when the user chooses what he want to do, e.g. place an order.
val ChooseAction: State = state(Interaction) {
    onEntry {
        furhat.ask("What can I help you with?")
    }
    //What happens on response nothing?

    onReentry {
        random(
                { furhat.ask("What can I help you with?")},
                {}
        )
    }

    //If the user wants to place an order
    onResponse<PlaceOrder> {
        val urlForPost = "http://127.0.0.1:9000/order"
        val values = mapOf("status" to "submitted")
        val postResponse = khttp.post(urlForPost, json=values)
        val currentId = postResponse.jsonObject["id"]
        goto(ListShoppingCategories(currentId))
    }

    onResponse<AddItem>{
        val urlForPost = "http://127.0.0.1:9000/order"
        val statusValue = mapOf("status" to "submitted")
        val idResponse = khttp.post(urlForPost, json=statusValue)
        val currentId = idResponse.jsonObject["id"]

        val postOrderUrl= "http://127.0.0.1:9000/order/$currentId/item_by_name"
        val name = CheckSynonyms(it.intent.item?.item?.value!!, "item")
        val values = mapOf("name" to name, "quantity" to it.intent.item?.count.toString().toInt())
        val postResponse = post(postOrderUrl, json=values)
        val chosenItem = it.intent.item
        //The chosen item should be added here
        furhat.say("${chosenItem?.count.toString()} ${chosenItem?.item.toString()} is added to your shopping order")
        goto(ChooseShoppingCartAction(currentId))
    }

    onResponse<ChooseCategory> {
        val urlForPost = "http://127.0.0.1:9000/order"
        val statusValue = mapOf("status" to "submitted")
        val idResponse = khttp.post(urlForPost, json=statusValue)
        val currentId = idResponse.jsonObject["id"]

        val name = CheckSynonyms(it.intent.category?.value!!, "category")
        val getItemsUrl = "http://127.0.0.1:9000/category_by_name/"+name
        val response = get(getItemsUrl).text
        val category = Gson().fromJson(response, CategoryData::class.java)
        furhat.say("The available items are")
        for(item in category.items!!) {
            furhat.say(item.name + " For the price of " + item.price)
        }
        if (it.intent.category != null){
            goto(AddShoppingItems(currentId))
        }
        else{
            furhat.say("That category does not exist")
            reentry()
        }
    }

    onResponse<Add> {
        val urlForPost = "http://127.0.0.1:9000/order"
        val statusValue = mapOf("status" to "submitted")
        val idResponse = khttp.post(urlForPost, json=statusValue)
        val currentId = idResponse.jsonObject["id"]
        goto(ListShoppingCategories(currentId))
    }

    //If the user wants to track an order
    onResponse<TrackOrder>{
        goto(GetOrderNumber("track"))
    }

    onResponse<ChangeOrder>{
        goto(GetOrderNumber("change"))
    }

    onResponse<CancelOrder>{
        goto(GetOrderNumber("cancel"))
    }

    onResponse<RequestOptions> {
        furhat.say("You can place an order, change an order, or track an order")
        reentry()
    }

    onResponse<GoodBye>{
        furhat.say("Have a nice day!")
        goto(Idle)
    }

    onResponse {
        random(
                {furhat.say("I'm sorry, I didn't catch that.")},
                {furhat.say("Sorry?")},
                {}
        )
        furhat.say("You can place an order, change an order, or track an order")
        reentry()
    }
}

val AnythingElse = state(Interaction){
    onEntry {
        furhat.ask("Is there anything else I can help you with today?")
    }

    onResponse<Yes> {
        goto(ChooseAction)
    }

    onResponse<No> {
        furhat.say("Have a nice day!")
        goto(Idle)
    }
}