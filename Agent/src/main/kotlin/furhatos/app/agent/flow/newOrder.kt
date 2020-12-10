package furhatos.app.agent.flow

import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
            val itemsInOrder = mutableListOf<String?>()
            for( item in order.items!!) {
                val itemQuantity = item.quantity.toString()
                val itemName = item.item?.name
                itemsInOrder.add("$itemQuantity $itemName")
            }
            furhat.say("Your shopping cart currently consist of")
            for (item in itemsInOrder){
                if (item != null) {
                    furhat.say ( item )
                }
            }
        }
        furhat.ask("What do you want to do with it?")
    }

    //onReentry{
     //   furhat.ask ("That is not an option, Your can checkout your order, add items, remove items, or aborting the order")
    //}

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
        goto(AddShoppingItems(currentId))
    }

    // Hampus Issue 3 <copy past the code from AddShoppingItems >
    onResponse<AddItem> {
        val orderByNameUrl= "http://127.0.0.1:9000/order/$currentId/item_by_name"
        val orderByIdUrl="http://127.0.0.1:9000/order/$currentId"

        val response = get(orderByIdUrl).text
        val order = Gson().fromJson(response, OrderData::class.java)
        val item_name = CheckSynonyms(it.intent.item?.item?.value!!, "item")
        for( item in order.items!!) {
            if (item.item?.name==item_name){
                val values = mapOf("name" to item_name.toLowerCase(), "quantity" to it.intent.item?.count?.value.plus(item.quantity))
                khttp.patch(orderByNameUrl, json=values )
                furhat.say("${it.intent.item?.count.toString()} ${item_name} is added to your shopping order")
                goto(ChooseShoppingCartAction(currentId))
            }
        }
        val values = mapOf("name" to item_name.toLowerCase(), "quantity" to it.intent.item?.count?.value)
        post(orderByNameUrl, json=values)
        furhat.say("${it.intent.item?.count.toString()} ${item_name} is added to your shopping order")
        goto(ChooseShoppingCartAction(currentId))
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
}

fun ListShoppingCategories(currentId: Any) = state(Interaction){
    onEntry {
        random(
                {furhat.ask ("Do you want me to list our item categories?")},
                {furhat.ask ("Shall I list the available item categories?")}
        )
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

fun ListShoppingItems(currentId: Any) = state(ListShoppingItemsByCategory(currentId)){
    onEntry {
        furhat.ask ("Are you interested in some category?")
    }

    //Prints out the list "Items" gotten from the nlu
    onResponse<Yes> {
        goto(ListShoppingItemsByCategory(currentId))
    }

    onResponse<No> {
        goto(AddShoppingItems(currentId))
    }
}

fun ListShoppingItemsByCategory(currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask ("What category are you interested in?")
    }

    //Prints out the list of items from the chosen category
    onResponse<ChooseCategory> {
        val name = CheckSynonyms(it.intent.category!!.value!!, "category")
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

    onResponse<No> {
        goto(AddShoppingItems(currentId))
    }
}

//Reads in the item(s) the user want to add to the list (currently from all items but should only be from the chosen category)
fun AddShoppingItems(currentId: Any): State = state(ChooseShoppingCartAction(currentId)){
    onEntry {
        furhat.ask("What items do you want to add to your order?")
    }

    onResponse<AddItem> {
        val orderByNameUrl= "http://127.0.0.1:9000/order/$currentId/item_by_name"
        val orderByIdUrl="http://127.0.0.1:9000/order/$currentId"

        val response = get(orderByIdUrl).text
        val order = Gson().fromJson(response, OrderData::class.java)
        val item_name = CheckSynonyms(it.intent.item?.item?.value!!, "item")
        for( item in order.items!!) {
            if (item.item?.name==item_name){
                val values = mapOf("name" to item_name.toLowerCase(), "quantity" to it.intent.item?.count?.value.plus(item.quantity))
                khttp.patch(orderByNameUrl, json=values )
                furhat.say("${it.intent.item?.count.toString()} ${item_name} is added to your shopping order")
                goto(ChooseShoppingCartAction(currentId))
            }
        }
        //val item_name = CheckSynonyms(it.intent.item?.item?.value!!, "item")
        val values = mapOf("name" to item_name.toLowerCase(), "quantity" to it.intent.item?.count?.value)
        post(orderByNameUrl, json=values)
        furhat.say("${it.intent.item?.count.toString()} ${item_name} is added to your shopping order")
        goto(ChooseShoppingCartAction(currentId))
    }
    onResponse<RequestOptions> {
        goto(ReDoCategoryList(currentId))
    }

    onResponse<RequestCategoriesOptions> {
        goto(ListShoppingCategories(currentId))
    }
    onResponse<RequestItemsOptions> {
        goto(ListShoppingItemsByCategory(currentId))
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
        val itemToRemove = it.intent.item?.item?.value?.toLowerCase()
        val quantityToRemove = it.intent.item?.count?.value
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = get(urlForGet).text
        val order = Gson().fromJson(response, OrderData::class.java)
        val itemsInOrder = mutableListOf<String?>()
        for( item in order.items!!) {
            itemsInOrder.add(item.item?.name)
        }
        for (item in order.items!!){
            if(item.item?.name==itemToRemove){
                if (item.quantity!! >=quantityToRemove!!) {
                    goto(ConfirmRemove(item.quantity, itemToRemove, quantityToRemove, currentId))
                }
                else{
                    val quantityInOrder = item.quantity
                    furhat.say("You only have $quantityInOrder $itemToRemove in your order")
                    reentry()
                }
                //goto(ChooseQuantityToRemove(itemToRemove, item.quantity, currentId))
            }
        }
        furhat.say("You have no "+itemToRemove.toString()+" in your shopping cart")
        reentry()
    }
    onResponse<RequestOptions> {
        furhat.say ("If you do not want to remove an item, your choices are to checkout your order, add an item, or aborting the order")
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

data class SynonymCheckData(
        val type: String? = null,
        val parent_category: String? = null,
        val parent_item: String? = null
)

fun CheckSynonyms(baseName: String, synonym_type: String? = "category"): String{


    val getSynonymUrl ="http://127.0.0.1:9000/synonym_checker"
    val jsonString = Gson().toJson(mapOf("synonym" to baseName))
    val getSynonyms = post(getSynonymUrl, data=jsonString).text
    val gson = GsonBuilder().serializeNulls().create()
    val synonymResponse = gson.fromJson(getSynonyms, SynonymCheckData::class.java)
    var name = ""
    if (getSynonyms != "{}"){
        if (synonym_type == "category"){
            name = synonymResponse.parent_category!!
        } else {
            name = synonymResponse.parent_item!!
        }

    } else {
        name = baseName
    }
    return name
}