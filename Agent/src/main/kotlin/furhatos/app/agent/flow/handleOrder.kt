package furhatos.app.agent.flow

import com.google.api.client.json.Json
import com.google.gson.Gson
import furhatos.app.agent.nlu.*
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.*
import furhatos.nlu.common.Number
import furhatos.snippets.Anything
import khttp.*

fun GetOrderNumber(action: String) = state(ChooseAction){
    onEntry {
        furhat.ask ( "What is your order number?" )
    }
    onResponse<InputTrackingNumber> {
        val currentId = it.intent.trackingNumber.toString()
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val response = get(urlForGet).text
        if (response=="{\"detail\":\"Item not found\"}") {
            furhat.say("An order with number $currentId does not exist")
            reentry()
        }
        else{
            val order = Gson().fromJson(response, OrderData::class.java)
            if (action=="track"){
                furhat.say ( "The order with number "+currentId+" is currently in the status "+order.status )
                goto(AnythingElse)
            }
            else if (action=="change"){
                if (order.status=="ready"){
                    furhat.say("Unfortunately your order is already ready for pick up and can not be changed")
                    goto(AnythingElse)
                }
                else{
                    val urlForGetOrder = "http://127.0.0.1:9000/order/$currentId"
                    val orderResponse = get(urlForGetOrder).text
                    val myOrder = Gson().fromJson(orderResponse, OrderData::class.java)
                    if (myOrder.items.isNullOrEmpty()){
                        furhat.say("Your order does not have any items")
                        goto(ChooseAction)
                    }
                    else {
                        goto(ChooseItemToChange(currentId,myOrder))
                    }
                }
            }
            else if (action=="cancel"){
                if (order.status=="ready"){
                    furhat.say("Unfortunately your order is already ready for pick up and can not be cancelled")
                    goto(AnythingElse)
                }
                else{
                    goto(ConfirmCancel(currentId))
                }
            }
            else{
                furhat.say(action)
            }
        }
    }
    onResponse<Aborting> {
        goto(ChooseAction)
    }
    onResponse<RequestOptions> {
        furhat.say("Specify your tracking number or abort the order tracking")
        reentry()
    }
}

fun ConfirmCancel(currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask("Are you sure you want to cancel order number $currentId ?")
    }

    onResponse<Yes>{
        val urlForDelete = "http://127.0.0.1:9000/order/$currentId"
        val response = delete(urlForDelete)
        furhat.say ("Order number $currentId has been cancelled")
        goto(AnythingElse)
    }

    onResponse<No> {
        furhat.say ("Your order har not been cancelled")
        goto(AnythingElse)
    }
}

fun ChooseItemToChange(currentId: Any, order: OrderData) = state(Interaction){
    onEntry {
        furhat.say("Your shopping cart currently consist of ")
        for( item in order.items!!) {
            furhat.say(""+item.quantity + " " + item.item?.name) //Hampus issue 2
        }
        furhat.ask("To change the quantity of an item, specify the item")

    }

    onResponse<ChangeItem> {
        val item = it.intent.item?.value
        goto(QuantityToKeep(item, currentId))
    }
}

fun QuantityToKeep(item: String?, currentId: Any) = state(Interaction){
    onEntry {
        furhat.ask("How many $item do you want to keep in your order")
    }

    onResponse<Number> {
        //val quantity=it.intent.toString().toInt()
        val quantity=it.intent.value
        val patchUrl = "http://127.0.0.1:9000/order/$currentId/item_by_name"
        //val values = mapOf("name" to item?.toLowerCase(), "quantity" to quantity)
        //val response = khttp.patch(patchUrl, json=values)
        val urlForGet = "http://127.0.0.1:9000/order/$currentId"
        val itemResponse = get(urlForGet).text
        val order = Gson().fromJson(itemResponse, OrderData::class.java)
        if (item !in (order.items?.map { it.item?.name } as ArrayList).toArray()){// suggestion by Hampus
            val values = mapOf("name" to item, "quantity" to quantity)
            post(patchUrl, json=values)
        }
        else {
            val values = mapOf("name" to item?.toLowerCase(), "quantity" to quantity)
            val response = khttp.patch(patchUrl, json=values)
        }

        if (order.items==null){
            furhat.say("The shopping cart is empty and can not be checked out!")
            goto(ChooseShoppingCartAction(currentId))
        }
        else{
            furhat.say ("Your order has been changed and your order number is $currentId")
            goto(AnythingElse)
        }
    }
}
