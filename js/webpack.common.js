const MiniCssExtractPlugin = require('mini-css-extract-plugin');
//const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');

module.exports = {
	entry: {
		"geo": './src/geo/index.js',
		"chart": './src/chart/index.js',
	},
	output: {
		filename: '[name].[contenthash].js',
		path: path.resolve(__dirname, 'org.argeo.app.js/org/argeo/app/js'),
		publicPath: '/pkg/org.argeo.app.js',
		clean: true,
	},
	optimization: {
		moduleIds: 'deterministic',
		runtimeChunk: 'single',
		// split code
		splitChunks: {
			chunks: 'all',
		},
		//		minimizer: [
		//			// For webpack@5 you can use the `...` syntax to extend existing minimizers (i.e. `terser-webpack-plugin`), uncomment the next line
		//			`...`,
		//			new CssMinimizerPlugin(),
		//		],
	},
	module: {
		rules: [
			{
				test: /\.(css)$/,
				use: [
					MiniCssExtractPlugin.loader,
					'css-loader',
				],
			},
		],
	},
	plugins: [
		// deal with CSS
		new MiniCssExtractPlugin(),
		// deal with HTML generation
		new HtmlWebpackPlugin({
			title: 'Argeo Suite Geo JS',
			template: 'src/geo/index.html',
			scriptLoading: 'module',
			filename: 'geo.html',
			chunks: ['geo'],
		}),
		new HtmlWebpackPlugin({
			title: 'Argeo Suite Chart JS',
			template: 'src/chart/index.html',
			scriptLoading: 'module',
			filename: 'chart.html',
			chunks: ['chart'],
		}),

	],
};