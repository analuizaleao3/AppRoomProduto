package br.unisanta.approomproduto.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import br.unisanta.approomproduto.dao.ProductDao
import br.unisanta.approomproduto.databinding.ActivityMainBinding
import br.unisanta.approomproduto.db.AppDatabase
import br.unisanta.approomproduto.model.Product
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o banco de dados Room de forma recomendada (sem usar allowMainThreadQueries)
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration() // Usado para recriar o DB se o esquema mudar
            .build()
        productDao = db.productDao()

        // Exemplo de inserção de produto
        binding.btnInserirProduto.setOnClickListener {
            val name = binding.editProductName.text.toString()
            val priceText = binding.editProductPrice.text.toString()
            val description = binding.editProductDescription.text.toString()
            val quantityText = binding.edtQuantidade.text.toString()

            // Verifica se os campos estão preenchidos
            if (name.isNotEmpty() && priceText.isNotEmpty()) {
                val price = priceText.toDouble() ?: 0.0
                val quantity = quantityText.toInt() ?: 0

                if (price <= 0.0)
                {
                    binding.editProductPrice.error = "Informe um preço válido"
                }
                else{
                    val product = Product(name = name, price = price, description = description, quantity = quantity)

                    // Insere o produto no banco de dados de forma assíncrona (usando corrotina)
                    lifecycleScope.launch {
                        productDao.insertAll(product)
                        Log.i("TAG", "Produto inserido: $name - R$$price")
                        Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()
                    }

                    binding.editProductName.text.clear()
                    binding.editProductPrice.text.clear()
                    binding.editProductDescription.text.clear()
                    binding.editProductName.requestFocus()
                }
            } else {
                Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            }
        }

        // Exemplo de recuperação de produtos
        binding.btnListarProdutos.setOnClickListener {

        // Recupera os produtos de forma assíncrona
            lifecycleScope.launch {
                val products = productDao.getAll()
                var texto = ""
                var tamanhoEstoque = products.size
                var totalPrecos = 0.0

                if (products.isNotEmpty()) {
                    products.forEach {
                        texto += "Produto = ${it.name}, Preço = R$${it.price}, Descrição = ${it.description} \n"
                        totalPrecos += it.price
                    }

                    binding.tvProdutos.text = "Quantidade Produtos: ${tamanhoEstoque} e Soma Preços: R$${String.format("%.3f", totalPrecos)} \n" + texto
                } else {
                    Log.i("TAG", "Nenhum produto encontrado.")
                }
            }
        }

        binding.btnBuscar.setOnClickListener{
            val name = binding.editProductName.text.toString()

            if (name.isNotEmpty()){
                lifecycleScope.launch {
                    val products = productDao.buscarPorNome("%$name%")
                    var texto = ""

                    products.forEach {
                        texto += "Id = ${it.id}, Produto = ${it.name}, Preço = R$${it.price}, Descrição = ${it.description} \n"
                    }

                    binding.tvProdutos.text = texto
                }
            }
            else {
                Toast.makeText(this, "Preencha o nome do produto!", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnExcluirTodos.setOnClickListener {
            lifecycleScope.launch {
                productDao.deleteAll()
                Toast.makeText(this@MainActivity, "Todos os produtos foram deletados com sucesso!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAtualizar.setOnClickListener{
            val idText = binding.edtId.text.toString()
            val priceText = binding.editProductPrice.text.toString()
            val id = idText.toInt() ?: 0
            val price = priceText.toDouble() ?: 0.0

            if (priceText.isNotEmpty()){
                lifecycleScope.launch {
                    productDao.atualizarProduto(id, price)
                    Toast.makeText(this@MainActivity, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this, "Preencha o id e o preço novo do produto!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}