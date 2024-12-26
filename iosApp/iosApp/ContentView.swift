import shared
import SwiftUI

struct ContentView: View {
	private let resolver = SpaydPayViaBankAppResolver()

	@State
	private var spayd = "SPD*1.0*ACC:CZ7603000000000076327632*AM:200.00*CC:CZK*X-VS:1234567890*MSG:CLOVEK V TISNI"

	var body: some View {
		VStack {
			TextField("SPAYD", text: $spayd)
				.padding()
				.font(.system(.body))
				.lineLimit(3)
				.multilineTextAlignment(.leading)
				.overlay(
					RoundedRectangle(cornerRadius: 8)
						.stroke(.tertiary, lineWidth: 2)
				)
				.clipShape(
					RoundedRectangle(cornerRadius: 8)
				)
				.padding()

			Button(action: {
				resolver.payViaBankApp(
					spayd: spayd,
					navigationParams: NavigationParameters()
				)
			}) {
				Label("Pay via bank app", systemImage: "banknote")
					.font(.system(.body))
			}
			.padding()
			.foregroundColor(.white)
			.background(Color.accentColor)
			.clipShape(Capsule())
			.padding()
		}
		.padding()
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
