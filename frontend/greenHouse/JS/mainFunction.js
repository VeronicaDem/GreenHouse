var myMap;

// Дождёмся загрузки API и готовности DOM.
ymaps.ready(init);

function init () {
    // Создание экземпляра карты и его привязка к контейнеру с
    // заданным id ("map").
    myMap = new ymaps.Map('map', {
        // При инициализации карты обязательно нужно указать
        // её центр и коэффициент масштабирования.
        center:[55.76, 37.64], // Москва
        zoom:10
    });
    
    //document.querySelectorAll('.mapHere')[0].innerHTML = myMap;
}

function switchMenu(){
    
    if (document.querySelectorAll('.downMenu')[0].style.display != 'none'){
        document.querySelectorAll('.downMenu')[0].style.display = 'none';
        document.querySelectorAll('.downMenuLeft')[0].style.display = 'block';
    } else {
        document.querySelectorAll('.downMenu')[0].style.display = 'block';
        document.querySelectorAll('.downMenuLeft')[0].style.display = 'none';
    }
}