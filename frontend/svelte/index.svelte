<script lang="ts">
import Button from './_button.svelte'


let isLoading = false
let succeeded = false
let email = ''
let error = ''

async function submit() {
  isLoading = true
  succeeded = false
  error = ''
  try {
    const resp = await fetch('/write', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({email, group: 'test-wait-list'})
    })

    if (resp.status === 200) {
      succeeded = true
    } else if (resp.status === 400) {
      const json = await resp.json()
      error = json.error
    } else {
      console.log(resp)
      error = 'Unknown error occurred.'
    }
  } catch (e) {
    console.error(e)
    error = 'Unexpected error occurred.'
  } finally {
    isLoading = false
  }
}

</script>

<div class="container mx-auto p-8 flex flex-col gap-2">
  <h1 class="text-3xl font-bold mb-2">Wait</h1>
  <div class="text-lg mb-4 italic">A headless wait list system that connects to Google Sheets. Self-hostable. No database required.</div>
  <div class="flex items-center gap-4">
    <div>
      <input
        type="email"
        class="input w-[300px]"
        placeholder="Email"
        disabled={isLoading || succeeded}
        bind:value={email}
      />
    </div>
    <Button {isLoading} disabled={succeeded} onClick={async () => {void submit()}}>
      {#if succeeded}
        You've joined the wait list.
      {:else}
        Join the wait list
      {/if}
    </Button>
  </div>
  {#if error}
    <div class="text-error text-sm">
      {error}
    </div>
  {/if}
</div>

<style lang="scss">
</style>
