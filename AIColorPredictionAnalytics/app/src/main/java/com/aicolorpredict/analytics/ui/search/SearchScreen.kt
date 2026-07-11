package com.aicolorpredict.analytics.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.ui.components.EmptyState
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.util.DateUtils

@Composable
fun SearchScreen(
    vm: SearchViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Search", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Text("Find rounds by ID, number, or pattern (e.g. \"3,7\" matches any round preceded by 3 then 7).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.mode == SearchMode.ROUND, onClick = { vm.setMode(SearchMode.ROUND) }, label = { Text("Round ID") })
            FilterChip(selected = state.mode == SearchMode.NUMBER, onClick = { vm.setMode(SearchMode.NUMBER) }, label = { Text("Number") })
            FilterChip(selected = state.mode == SearchMode.PATTERN, onClick = { vm.setMode(SearchMode.PATTERN) }, label = { Text("Pattern") })
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Query") },
            placeholder = {
                Text(when (state.mode) {
                    SearchMode.ROUND -> "e.g. 42"
                    SearchMode.NUMBER -> "0..9"
                    SearchMode.PATTERN -> "e.g. 3,7"
                })
            }
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.search() }, modifier = Modifier.fillMaxWidth()) { Text("Search") }
        Spacer(Modifier.height(12.dp))

        if (state.errorMessage != null) {
            GlassCard { Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        } else if (state.results.isEmpty() && !state.isLoading) {
            EmptyState(title = "No results", subtitle = "Try a different query.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.results, key = { it.id }) { r ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("#${r.id}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(DateUtils.displayShort(r.epochMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Number ${r.number}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
