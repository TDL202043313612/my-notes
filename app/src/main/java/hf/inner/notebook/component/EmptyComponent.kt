package hf.inner.notebook.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.Text
import hf.inner.notebook.R

@Composable
fun EmptyComponent(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_empty), contentDescription = null)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NoThing ~",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            )
        }
    }
}