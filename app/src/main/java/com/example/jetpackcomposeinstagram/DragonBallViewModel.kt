package com.example.jetpackcomposeinstagram

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetpackcomposeinstagram.model.DragonBall
import com.example.jetpackcomposeinstagram.model.Item
import com.example.jetpackcomposeinstagram.network.DragonBallApi
import kotlinx.coroutines.launch

class DragonBallViewModel: ViewModel() {
    private val api = DragonBallApi.service

    private val _characters = MutableLiveData<List<Item>>() // Aquí debe ser una lista de Items
    val characters: LiveData<List<Item>> = _characters

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getAllCharacters() {
        viewModelScope.launch {
            try {
                val response = api.getCharacters()
                if (response.isSuccessful) {
                    _characters.value = response.body()?.items ?: emptyList() // Asigna la lista de personajes
                } else {
                    _error.value = "Error: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun getCharacter(characterId: String): LiveData<Item> {
        val _character = MutableLiveData<Item>()
        viewModelScope.launch {
            try {
                val response = api.getCharacter(characterId)
                if (response.isSuccessful) {
                    _character.value = response.body()
                } else {
                    _error.value = "Error: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
        return _character
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun DragonBallScreen(
    viewModel: DragonBallViewModel = DragonBallViewModel(), navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.getAllCharacters()
    }

    val characters by viewModel.characters.observeAsState()
    val error by viewModel.error.observeAsState()

    if (error != null) {
        Text(text = error!!)
    } else if (characters != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            itemsIndexed(characters!!) { index, character -> // Aquí characters es la lista de Items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate("detail/${character.id}")
                        }

                ) {
                    FullImageFromURLWithPlaceHolder(character.image)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "ID: ${character.id}",
                            fontSize = 30.sp
                        )
                        Text(
                            text = character.name,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Strength: ${character.ki}",
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FullImageFromURLWithPlaceHolder(imageUrl: String){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_dots),
        contentDescription = "Texto Imagen",
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(100.dp)
    )
}

@Composable
fun DetailScreen(
    viewModel: DragonBallViewModel = DragonBallViewModel(),
    navController: NavController,
    characterId: String
) {
    val character by viewModel.getCharacter(characterId).observeAsState()

    if (character != null) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa toda la pantalla
                .padding(16.dp),
            verticalArrangement = Arrangement.Center, // Centra los elementos verticalmente
            horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente
        ) {
            DisplayImageDetail(character!!.image)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "ID: ${character!!.id}",
                fontSize = 30.sp
            )
            Text(
                text = character!!.name,
                fontSize = 15.sp
            )
            Text(
                text = "Strength: ${character!!.ki}",
                fontSize = 15.sp
            )
            // Botón de "volver" en la esquina superior izquierda
            IconButton(
                onClick = { navController.popBackStack() }, // Vuelve a la pantalla anterior
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // Usa el ícono de flecha hacia atrás
                    contentDescription = "Volver"
                )
            }


        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Centra el CircularProgressIndicator
        ) {
            CircularProgressIndicator()
        }

    }
}

@Composable
fun DisplayImageDetail(imageUrl: String){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_dots),
        contentDescription = "Texto Imagen",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    )
}



