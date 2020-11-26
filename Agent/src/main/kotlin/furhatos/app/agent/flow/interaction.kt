package furhatos.app.agent.flow

import furhatos.app.agent.nlu.*
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.*

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
val ChooseAction = state(Interaction) {
    onEntry {
        furhat.ask("What can I help you with?")
    }
    //What happens on response nothing?

    //If the user wants to place an order
    onResponse<PlaceOrder> {
        //--->Create an empty shopping cart here<---
        goto(ChooseShoppingCartAction)
    }

    onResponse<RequestOptions> {
        furhat.say("You can place an order, change an order, or track an order")
        reentry()
    }

    onResponse<GoodBye>{
        furhat.say("So long")
        goto(Idle)
    }

    //onResponse {
    //    furhat.say("I'm sorry, I didn't catch that.")
    //    furhat.say("You can place an order, change an order, or track an order")
    //    reentry()
    //}
}


val ChooseShoppingCartAction = state(Interaction){
    onEntry {
        //Get ShoppingCart.items from API
        //if (ShoppingCart.items.isEmpty()){
            furhat.say("Your shopping cart is currently empty")
        //}
        //else{
        //    furhat.say {
        //        +"Your shopping cart currently consist of"
        //        //Get ShoppingCart.items from API
        //        +println(ShoppingCart.items) }
        //}
        furhat.ask("What do you want to do with it?")
    }

    //The user wants to checkout (place) his order
    onResponse<Checkout> {
        //Get ShoppingCart.items from API
        //if(ShoppingCart.items.isEmpty()){
        //    furhat.say{"The shopping cart is empty and can not be checked out!"}
        //    furhat.say {"Your choices are to add items, or aborting the order"}
        //    reentry()
        //}
        //else{
        //    furhat.say {
        //        +"Your order has been placed and your order number is "
                //Get ShoppingCart.id from API
        //        +ShoppingCart.id
        //        +"Have a nice day!"
        //    }
            goto(Idle)
        //}
    }
    onResponse<Add> {
        goto(ListShoppingCategories)
    }

    //Maybe change instead of remove in nlu?
    onResponse<Remove> {
        //if (ShoppingCart.items.isEmpty()){
            furhat.say ( "Your shopping cart is empty, you can't change an item in it." )
            reentry()
        //}
        //else {
        //    goto(ChooseItemToRemove)
        //}
    }
    onResponse<Aborting> {
        furhat.say("Order aborted")
        //goto(ChooseAction)
    }
    onResponse<RequestOptions> {
        furhat.say ("Your choices are to checkout your order, add items, change items, or aborting the order")
        reentry()
    }
    onResponse {
        furhat.say ("That is not an option, if you want you can ask me to list your options")
        reentry()
    }
}

val ListShoppingCategories = state(Interaction){
    onEntry {
        furhat.ask ("Do you want me to list the available categories of items in the store?")
    }

    //Prints out the list "Category" gotten from the nlu
    onResponse<Yes> {
    //    furhat.say {
    //        +"The available categories are"
    //        +Category
    //    }
        goto(ListShoppingItems)
    }

    onResponse<No> {
        goto(ListShoppingItems)
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

val ListShoppingItems = state(Interaction){
    onEntry {
        furhat.ask ("Do you want me to list the available items in the store?")
    }

    //Prints out the list "Items" gotten from the nlu
    onResponse<Yes> {
        furhat.say {
    //        +"The available items are ${Item().optionsToText}"
        }
        goto(AddShoppingCategory)
    }

    onResponse<No> {
        goto(AddShoppingCategory)
    }

    onResponse {
        furhat.say ( "Sorry, I did not catch that" )
        reentry()
    }
}

//Reads in the category the user wants to add an item from
val AddShoppingCategory: State = state(ChooseShoppingCartAction){
    onEntry {
        furhat.ask("From what category do you want to add an item?")
    }

    onResponse<ChooseCategory> {
        //Update ShoppingCart in the API
        val chosenCategory = it.intent.category
        if (chosenCategory != null){
            goto(AddShoppingItems(chosenCategory))
        }
        else{
            propagate()
        }
    }
    onResponse<RequestOptions> {
        furhat.say {"The available categories are ${Category().optionsToText()}"}
        furhat.say ("If you do not want to add more items, your choices are to checkout your order, change items, or aborting the order")
        reentry()
    }
}

//Reads in the item(s) the user want to add to the list (currently from all items but should only be from the chosen category)
fun AddShoppingItems(category: Category) = state(ChooseShoppingCartAction){
    onEntry {
        furhat.ask("What items in the category ${category.text} do you want to add to the list?")
    }

    onResponse<AddItem> {
        //Update ShoppingCart in the API
        //ShoppingCart.items.add.item=item
        //ShoppingCart.items.add.quantity=quantity
        goto(ChooseShoppingCartAction)
    }
    onResponse<RequestOptions> {
        //furhat.say {"The available items in the category ${category.text} are"+store.items(category)}"}
        furhat.say ("If you do not want to add more items, your choices are to checkout your order, change items, or aborting the order")
        reentry()
    }
    //onResponse<ChooseCategory>{
    //    val newCategory = it.intent.category
    //    if (newCategory != null){
    //        goto(AddShoppingItems(newCategory))
    //    }
    //    else{
    //        propagate()
    //    }
    //}
}

val ChooseItemToRemove = state(ChooseShoppingCartAction){
    onEntry {
        furhat.ask("What item do you want remove?")
    }
    onResponse<RemoveItem> {
       // val itemToRemove = it.intent.itemsInCart
        //Remove item from order here
        //        ...
        //
        goto(ChooseShoppingCartAction)
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

