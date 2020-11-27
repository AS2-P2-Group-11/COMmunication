package furhatos.app.agent.flow

import com.google.gson.Gson
import furhatos.app.agent.nlu.*
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.*

data class OrderData (
        var status: String? = null,
        var id: Int? = null,
        var date: String? = null,
        var items: List<ItemData>? = null
)

//Greeting
val Start = state(Interaction) {
    onEntry {
        random(
                {   furhat.say("Whazzup dawg?!") },
                {   furhat.say("Greetings! What an absolute pleasure to meet you.") }
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

    //If the user wants to place an order
    onResponse<PlaceOrder> {
        val urlForPost = "http://127.0.0.1:9000/order"
        val values = mapOf("status" to "submitted")
        val postResponse = khttp.post(urlForPost, json=values)
        val currentId = postResponse.jsonObject["id"]
        goto(ChooseShoppingCartAction(currentId))
    }

    onResponse<RequestOptions> {
        furhat.say("You can place an order, change an order, or track an order")
        reentry()
    }

    onResponse<GoodBye>{
        furhat.say("Goodbye!")
        goto(Idle)
    }

    onResponse {
        furhat.say("I'm sorry, I didn't catch that.")
        furhat.say("You can place an order, change an order, or track an order")
        reentry()
    }
}

fun ChooseShoppingCartAction(currentId: Any): State = state(Interaction){
    onEntry {

        //Get current items of your order from API
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = khttp.get(urlForGet).text
        val order = Gson().fromJson(response, OrderData::class.java)
        println(order)
        if (order.items==null){
            furhat.say("Your shopping cart is currently empty")
        }
        else{
            furhat.say ("Your shopping cart currently consist of"+order.items)
        }
        furhat.ask("What do you want to do with it?")
    }

    //The user wants to checkout (place) his order
    onResponse<Checkout> {
        //Get current items of your order from API
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = khttp.get(urlForGet).text
        val order = Gson().fromJson(response, OrderData::class.java)
        if (order.items==null){
                furhat.say("The shopping cart is empty and can not be checked out!")
                furhat.say ("Your choices are to add items, or aborting the order")
                reentry()
        }
        else{
            furhat.say ( "Your order has been placed and your order number is "+currentId+"Have a nice day!")
            goto(Idle)
        }
    }
    onResponse<Add> {
        goto(ListShoppingCategories(currentId))
    }

    //Maybe change instead of remove in nlu?
    onResponse<Remove> {
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = khttp.get(urlForGet).text
        val order = Gson().fromJson(response, OrderData::class.java)
        if (order.items==null){
            furhat.say ( "Your shopping cart is empty, you can't remove an item from it." )
            reentry()
        }
        else {
            goto(ChooseItemToRemove(currentId))
        }
    }
    onResponse<Aborting> {
        val urlForDelete = "http://127.0.0.1:9000/order/$currentId"
        val deleteResponse = khttp.delete(urlForDelete)
        furhat.say("Order aborted")
        goto(ChooseAction)
    }
    onResponse<RequestOptions> {
        furhat.say ("Your choices are to checkout your order, add items, remove items, or aborting the order")
        reentry()
    }
    onResponse {
        furhat.say ("That is not an option, if you want you can ask me to list your options")
        reentry()
    }
}

fun ListShoppingCategories(currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask ("Do you want me to list the available categories of items in the store?")
    }

    //Prints out the list "Category" gotten from the nlu
    onResponse<Yes> {
        val getCategoriesUrl = "http://127.0.0.1:9000/categories"
        val response = khttp.get(getCategoriesUrl).text
        val categories = Gson().fromJson(response, CategoryList::class.java)
        furhat.say ( "The available categories are" )
        for (category in categories){
            furhat.say(category.name)
        }
        goto(ListShoppingItems(currentId))
    }

    onResponse<No> {
        goto(AddShoppingCategory(currentId))
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

fun ListShoppingItems(currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask ("Do you want me to list the available items in the store?")
    }

    //Prints out the list "Items" gotten from the nlu
    onResponse<Yes> {
        val getCategoriesUrl = "http://127.0.0.1:9000/categories"
        val response = khttp.get(getCategoriesUrl).text
        val categories = Gson().fromJson(response, CategoryList::class.java)
        furhat.say("The available items are")
        for (category in categories){
            furhat.say("In the category of "+category.name)
            for(item in category.items!!) {
                furhat.say(item.title + "For the price of" + item.price)
            }
        }
        goto(AddShoppingCategory(currentId))
    }

    onResponse<No> {
        goto(AddShoppingCategory(currentId))
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

//Reads in the category the user wants to add an item from
fun AddShoppingCategory(currentId: Any): State = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("From what category do you want to add items?")
    }

    onResponse<ChooseCategory> {
        val chosenCategory = it.intent.category
        if (chosenCategory != null){
            goto(AddShoppingItems(chosenCategory, currentId))
        }
        else{
            furhat.say("That category does not exist")
            reentry()
        }
    }
    onResponse<RequestOptions> {
        val getCategoriesUrl = "http://127.0.0.1:9000/categories"
        val response = khttp.get(getCategoriesUrl).text
        val categories = Gson().fromJson(response, CategoryList::class.java)
        furhat.say ("The available categories are")
        for (category in categories){
            furhat.say(category.name)
        }
        furhat.say ("If you do not want to add more items, your choices are to checkout your order, change items, or aborting the order")
        reentry()
    }
}


//Reads in the item(s) the user want to add to the list (currently from all items but should only be from the chosen category)
fun AddShoppingItems(category: Category, currentId: Any) = state(AddShoppingCategory(currentId)){
    onEntry {
        furhat.ask("What items in the category"+category+"do you want to add to your order?")
    }

    onResponse<AddItem> {
        val postOrderUrl= "http://127.0.0.1:9000/order/$currentId/item_by_title"
        val values = mapOf("title" to it.intent.item)
        val postResponse = khttp.post(postOrderUrl, json=values)
        val chosenItem = it.intent.item
        //The chosen item should be added here
        furhat.say("...was added to your shopping order")
        goto(ChooseShoppingCartAction(currentId))
    }
    onResponse<RequestOptions> {
        //The category should here be something like category.name and items, category.items.title
        furhat.say ("The available items in the category"+category+"are ...")
        furhat.say ("If you do not want to add more items, your choices are to checkout your order, change items, or aborting the order")
        reentry()
    }
}

fun ChooseItemToRemove(currentId: Any) = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("What item do you want remove?")
    }
    onResponse<RemoveItem> {
        val itemToRemove = it.intent.item
        //Remove item from order here
        //        ...
        //
        goto(ChooseShoppingCartAction(currentId))
    }
    onResponse<RequestOptions> {
        furhat.say ("If you do not want to change or remove an item, your choices are to checkout your order, add an item, or aborting the order")
        reentry()
    }
    onResponse{
        furhat.say("I'm sorry, I did not catch that")
        reentry()
    }
}

