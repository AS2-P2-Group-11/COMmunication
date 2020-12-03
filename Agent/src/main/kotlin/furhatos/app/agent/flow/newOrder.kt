package furhatos.app.agent.flow

import com.google.gson.Gson
import furhatos.app.agent.nlu.*
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.*
import furhatos.nlu.common.Number
import khttp.*

fun ChooseShoppingCartAction(currentId: Any): State = state(Interaction){
    onEntry {

        //Get current items of your order from API
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response1 = get(urlForGet)
        val response = get(urlForGet).text
        val order = Gson().fromJson(response, OrderData::class.java)
        if (order.items.isNullOrEmpty()){
            furhat.say("Your shopping cart is currently empty")
        }
        else{
            for( item in order.items!!) {
                furhat.say("Your shopping cart currently consist of "+item.quantity+" "+item.item?.name)
            }
        }
        furhat.ask("What do you want to do with it?")
    }

    //The user wants to checkout (place) his order
    onResponse<Checkout> {
        //Get current items of your order from API
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = get(urlForGet).text
        val order = Gson().fromJson(response, OrderData::class.java)
        if (order.items==null){
            furhat.say("The shopping cart is empty and can not be checked out!")
            furhat.say ("Your choices are to add items, or aborting the order")
            reentry()
        }
        else{
            furhat.say ("Your order has been placed and your order number is $currentId")
            goto(AnythingElse)
        }
    }
    onResponse<Add> {
        goto(ListShoppingCategories(currentId))
    }

    //Maybe change instead of remove in nlu?
    onResponse<Remove> {
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = get(urlForGet).text
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
        val deleteResponse = delete(urlForDelete)
        furhat.say("Order aborted")
        goto(ChooseAction)
    }
    onResponse<RequestOptions> {
        furhat.say ("Your choices are to checkout your order, add items, remove items, or aborting the order")
        reentry()
    }
    onResponse {
        furhat.ask ("That is not an option, Your can checkout your order, add items, remove items, or aborting the order")
        //reentry()
    }
}

fun ListShoppingCategories(currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask ("Do you want me to list the available categories of items in the store?")
    }

    //Prints out the list "Category" gotten from the nlu
    onResponse<Yes> {
        val getCategoriesUrl = "http://127.0.0.1:9000/categories"
        val response = get(getCategoriesUrl).text
        val categories = Gson().fromJson(response, CategoryList::class.java)
        furhat.say ( "The available categories are" )
        for (category in categories){
            furhat.say(category.name)
        }
        goto(ListShoppingItems(currentId))
    }

    onResponse<No> {
        goto(AddShoppingItems(currentId))
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

fun ListShoppingItems(currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask ("Do you want me to list the available items in some category?")
    }

    //Prints out the list "Items" gotten from the nlu
    onResponse<Yes> {
        goto(ListShoppingItemsByCategory(currentId))
    }

    onResponse<No> {
        goto(AddShoppingItems(currentId))
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

fun ListShoppingItemsByCategory(currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask ("What category are you interested in?")
    }

    //Prints out the list of items from the chosen category
    onResponse<ChooseCategory> {
        val getItemsUrl = "http://127.0.0.1:9000/category_by_name/"+it.intent.category?.value
        val response = get(getItemsUrl).text
        println(response)
        val category = Gson().fromJson(response, CategoryData::class.java)
        println(category)
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

    onResponse<No> {
        goto(AddShoppingItems(currentId))
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

//Reads in the item(s) the user want to add to the list (currently from all items but should only be from the chosen category)
fun AddShoppingItems(currentId: Any): State = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("What items do you want to add to your order?")
    }

    onResponse<AddItem> {
        val postOrderUrl= "http://127.0.0.1:9000/order/$currentId/item_by_name"
        val values = mapOf("name" to it.intent.item?.item?.value?.toLowerCase(), "quantity" to it.intent.item?.count?.value)
        val postResponse = post(postOrderUrl, json=values)
        val chosenItem = it.intent.item
        //The chosen item should be added here
        furhat.say("${it.intent.item?.count.toString()} ${it.intent.item?.item.toString()} is added to your shopping order")
        goto(ChooseShoppingCartAction(currentId))
    }
    onResponse<RequestOptions> {
        goto(ReDoCategoryList(currentId))
    }
    onResponse {
        furhat.ask ("That is not an option, if you do not want to add items you can checkout your order, remove items, or aborting the order")
        reentry()
    }
}

fun ReDoCategoryList(currentId: Any) = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("Do you want me to list the available categories and items in the store?")
    }

    onResponse<Yes> {
        goto(ListShoppingCategories(currentId))
    }

    onResponse<No> {
        furhat.say ("If you do not want to add more items, your choices are to checkout your order, change items, or aborting the order")
        goto(AddShoppingItems(currentId))
    }
}

fun ChooseItemToRemove(currentId: Any) = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("What item do you want remove?")
    }
    onResponse<RemoveItem> {
        val itemToRemove = it.intent.item?.value?.toLowerCase()
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = get(urlForGet).text
        val order = Gson().fromJson(response, OrderData::class.java)
        val itemsInOrder = mutableListOf<String?>()
        for( item in order.items!!) {
            itemsInOrder.add(item.item?.name)
        }
        for (item in order.items!!){
            if(item.item?.name==itemToRemove){
                goto(ChooseQuantityToRemove(itemToRemove, item.quantity, currentId))
            }
        }
        furhat.say("You have no "+itemToRemove.toString()+" in your shopping cart")
        reentry()
        //furhat.say("Your shopping cart currently consist of "+order.items.item.quantity+" "+item.item?.name)
        //goto(ChooseQuantityToRemove(itemToRemove,currentId))
    }
    onResponse<RequestOptions> {
        furhat.say ("If you do not want to remove an item, your choices are to checkout your order, add an item, or aborting the order")
        reentry()
    }
}

fun ChooseQuantityToRemove(itemToRemove: String?, quantityInOrder: Int?, currentId: Any) = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("Your order currently has $quantityInOrder $itemToRemove , how many do you want to remove?")
    }
    onReentry {
        furhat.ask("How many $itemToRemove do you want to remove?")
    }
    onResponse<Number> {
        if(it.intent.value!! > quantityInOrder!!){
            furhat.say("You only have $quantityInOrder $itemToRemove in your order")
            reentry()
        }
        else{
           goto(ConfirmRemove(quantityInOrder, itemToRemove, it.intent.value, currentId))
        }
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

fun ConfirmRemove(quantityInOrder: Int?, itemToRemove: String?, quantityToRemove: Int?, currentId: Any): State = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("Are you certain you want to remove $quantityToRemove $itemToRemove from your order?")
    }
    onResponse<Yes> {
        val patchUrl = "http://127.0.0.1:9000/order/$currentId/item_by_name"
        val values = mapOf("name" to itemToRemove, "quantity" to quantityInOrder!!-quantityToRemove!!)
        khttp.patch(patchUrl, json=values )
        goto(ChooseShoppingCartAction(currentId))
    }
    onResponse<No> {
        goto(ChooseItemToRemove(currentId))
    }
}
